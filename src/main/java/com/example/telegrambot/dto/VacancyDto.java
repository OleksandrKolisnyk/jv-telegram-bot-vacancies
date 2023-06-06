package com.example.telegrambot.dto;

import com.opencsv.bean.CsvBindByName;

public class VacancyDto {
    @CsvBindByName(column = "Id")
    private String id;

    @CsvBindByName(column = "Title")
    private String title;

    @CsvBindByName(column = "Short description")
    private String shortDescription;

    @CsvBindByName(column = "Long description")
    private String longDescription;

    @CsvBindByName(column = "Company")
    private String company;

    @CsvBindByName(column = "Salary")
    private String salary;

    @CsvBindByName(column = "Link")
    private String link;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return
                // "VacancyDto{" +
                "id: " + id + "\n" +
                "title: " + title + "\n" +
                "Company: " + company + "\n" +
                "Salary: " + salary + "\n" +
                "Link: " + link + "\n\n" +
                "Description:" + "\n" + shortDescription + "\n\n" +
                "FullDescription: " + "\n\n" + longDescription + "\n\n"
                //+ '}'
                ;
    }
}
