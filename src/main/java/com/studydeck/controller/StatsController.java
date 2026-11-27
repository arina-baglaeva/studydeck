package com.studydeck.controller;

import com.studydeck.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("totalSessions", statsService.totalSessions());
        model.addAttribute("modeStats", statsService.modeStats());
        model.addAttribute("recentSessions", statsService.recentSessions());
        return "stats";
    }
}
