package com.hkp.flowershop.controller;

import com.hkp.flowershop.dto.requests.ChatRequest;
import com.hkp.flowershop.dto.response.ChatResponse;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.service.ai.ChatService;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ResponseEntity<?> sendMessage(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        try {
            String reply = chatService.chat(authentication.getName(), request.getMessage());
            return ResponseUtil.success(new ChatResponse(reply));
        } catch (BadRequestException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error while processing chat message", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        try {
            return ResponseUtil.success(chatService.getHistory(authentication.getName()));
        } catch (Exception e) {
            log.error("Error while fetching chat history", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @DeleteMapping("/history")
    public ResponseEntity<?> clearHistory(Authentication authentication) {
        try {
            chatService.clearHistory(authentication.getName());
            return ResponseUtil.success("Chat history cleared");
        } catch (Exception e) {
            log.error("Error while clearing chat history", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }
}
