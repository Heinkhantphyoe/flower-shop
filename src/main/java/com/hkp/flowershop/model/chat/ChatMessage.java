package com.hkp.flowershop.model.chat;

import java.time.LocalDateTime;

public record ChatMessage(String sender, String content, LocalDateTime timestamp) {
}
