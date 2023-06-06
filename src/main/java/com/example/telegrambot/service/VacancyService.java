package com.example.telegrambot.service;

import com.example.telegrambot.dto.VacancyDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VacancyService {
    @Autowired
    private VacanciesReaderService vacanciesReaderService;
    private final Map<String, VacancyDto> vacancies = new HashMap<>();

    @PostConstruct
    public void init() {
        List<VacancyDto> vacancyList = vacanciesReaderService.getVacanciesFromFile("vacancies.csv");
        for (VacancyDto vacancy : vacancyList) {
            vacancies.put(vacancy.getId(), vacancy );
        }
    }

    public List<VacancyDto> getVacanciesOfLevel(String vacancyLevel) {
        return vacancies.values().stream()
                .filter(v -> v.getTitle().toLowerCase().contains(vacancyLevel.toLowerCase()))
                .toList();
    }

    public List<VacancyDto> getVacanciesWithOutLevel(String[] vacancyLevels) {
        Set<String> vacancyLevelSet = Arrays.stream(vacancyLevels).collect(Collectors.toSet());
        return vacancies.values().stream()
                .filter(v ->
                        vacancyLevelSet.stream()
                                .noneMatch(vl -> v.getTitle().toLowerCase().contains(vl.toLowerCase())
                        )
                )
                .collect(Collectors.toList());
    }

    public VacancyDto get(String id) {
        return vacancies.get(id);
    }

}
