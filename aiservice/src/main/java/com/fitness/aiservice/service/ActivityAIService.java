package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {
    private final GeminiService geminiService;
    public String generateRecommendation(Activity activity){
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        log.info("AI Response is {}", aiResponse);
        return aiResponse;
    }

    private String createPromptForActivity(Activity activity){
        return String.format("""
                Analyze this fitness activity and provide detailed recommendations in the following exact JSON format:
                {
                "analysis": {
                "overall":"Over all analysis here",
                "pace":"Pace analysis here",
                "heartRate":"Heart rate analysis here",
                "caloriesBurned":"Calories burned analysis here"
                },
                "improvements":[
                 {
                  "area":"Area Name",
                  "recommendation":"Detailed Recommendation"
                 }
                ],
                "suggestions":[
                 {
                  "workout":"Workout Name",
                  "description":"Detailed Workout Description"
                 }
                ],
                "safety":[
                "Safety point 1"
                "Safety point 2"
                ]
                }
                
                Analyze this activity:
                Activity Type:%s
                Duration: %d minutes
                Calories Burned: %d
                Additional Metrics: %s
                
                Provide detailed analysis focusing on performance, improvements, next workout suggestions and safety guidelines.
                Ensure that response follows the exact JSON format shown above.
                """,
                          activity.getType(),
                          activity.getDuration(),
                          activity.getCaloriesBurned(),
                          activity.getAdditionalMetrics()
        );
    }
}
