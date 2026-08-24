package com.hkp.flowershop.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.model.chat.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
            You are "Bloom", the friendly virtual assistant of an online flower shop.
            Your name is Bloom. Always stay polite, warm and concise (2-3 sentences maximum).

            Rules:
            - Only discuss shop-related topics: flowers, bouquets, prices, availability, categories, delivery, and the customer's orders.
            - ALWAYS use the provided tools to check product availability, prices and order status. Never invent products or prices.
            - If a product is out of stock, apologize and suggest alternatives from the search results.
            - Prices are in USD. Shipping is a flat $5 fee. Same-day delivery applies to orders placed before 2 PM.
            - Order statuses: PENDING (awaiting confirmation), CONFIRMED (being prepared), DELIVERED, CANCELLED.
            - If the customer asks about something unrelated to the shop, politely redirect the conversation back to flowers.

            The customer you are talking to is named %s.
            """;

    private final ChatClient chatClient;
    private final ShopTools shopTools;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chatbot.session.ttl-hours:24}")
    private long ttlHours;

    @Value("${chatbot.history.max-messages:20}")
    private int maxHistoryMessages;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       ShopTools shopTools,
                       StringRedisTemplate redisTemplate,
                       ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.shopTools = shopTools;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String chat(String userEmail, String message) {
        String sessionKey = sessionKey(userEmail);
        List<ChatMessage> history = readHistory(sessionKey);

        String reply = callModel(message, history);

        history.add(new ChatMessage("user", message, LocalDateTime.now()));
        history.add(new ChatMessage("bot", reply, LocalDateTime.now()));
        if (history.size() > maxHistoryMessages) {
            history = new ArrayList<>(history.subList(history.size() - maxHistoryMessages, history.size()));
        }
        writeHistory(sessionKey, history);
        return reply;
    }

    public List<ChatMessage> getHistory(String userEmail) {
        return readHistory(sessionKey(userEmail));
    }

    public void clearHistory(String userEmail) {
        redisTemplate.delete(sessionKey(userEmail));
    }

    private String callModel(String message, List<ChatMessage> history) {
        try {
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT.formatted(customerName()))
                    .messages(toAiMessages(history))
                    .user(message)
                    .tools(shopTools)
                    .call()
                    .content();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Chat completion failed: {}", e.getMessage(), e);
            throw new BadRequestException("The assistant is unavailable right now. Please try again later.");
        }
    }

    private String customerName() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "customer";
    }

    private List<Message> toAiMessages(List<ChatMessage> history) {
        List<Message> messages = new ArrayList<>();
        for (ChatMessage m : history) {
            if (m.content() == null || m.content().isBlank()) {
                continue;
            }
            if ("user".equals(m.sender())) {
                messages.add(new UserMessage(m.content()));
            } else {
                messages.add(new AssistantMessage(m.content()));
            }
        }
        return messages;
    }

    private String sessionKey(String userEmail) {
        return "chat:session:" + userEmail;
    }

    private List<ChatMessage> readHistory(String key) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(Arrays.asList(objectMapper.readValue(json, ChatMessage[].class)));
        } catch (JsonProcessingException e) {
            log.warn("Could not parse chat history for {}, starting fresh", key);
            return new ArrayList<>();
        }
    }

    private void writeHistory(String key, List<ChatMessage> history) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(history), Duration.ofHours(ttlHours));
        } catch (JsonProcessingException e) {
            log.error("Could not persist chat history for {}", key, e);
        }
    }
}
