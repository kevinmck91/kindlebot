package com.kevinmck91.kindlebot.config;

import org.springframework.stereotype.Service;

@Service
public class FirebaseStatusService {

    private boolean available = false;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
