package com.example.telegrambot;

import com.example.telegrambot.dto.VacancyDto;
import com.example.telegrambot.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VacanciesBot extends TelegramLongPollingBot {
    @Autowired
    private VacancyService vacancyService;
    public VacanciesBot() {
        super("5977266060:AAHURrehGRRMjm3Jjuy-0Xaycrc6DE4SPos");
    }

    private final String[] vacancyLevels = {"Junior", "Middle", "Senior"};

    private Map<Long, String> lastShowVacancyLevel = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.getMessage() != null) {
                handleStartCommand(update);
            }
            if (update.getCallbackQuery() != null) {
                String callBackData = update.getCallbackQuery().getData();
                System.out.println("callBackData: " + callBackData);

                if (callBackData.startsWith("showVacanciesOf")) {
                    String vacancyLevel = callBackData.split("Of")[1];
                    showVacanciesOfLevel(vacancyLevel, update);
/*
                    if (vacancyLevel.equals(vacancyLevels[0]))
                        showJuniorVacancies(update);
*/
                } else if (callBackData.startsWith("vacancyId=")) {
                    String id = callBackData.split("=")[1];
                    showVacancyDescription(id, update);
                } else if (callBackData.startsWith("backTo")) {
                    // String menuLevel = callBackData.split("To")[1];
                    // add handlerBackToMenuCommand (menuLevel, update);
                    if (callBackData.equals("backToVacancies")) {
                        handleBackToVacanciesCommand(update);
                    } else if (callBackData.equals("backToStartMenu")) {
                        handleBackToStartMenuCommand(update);
                    }

                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't send message to user!", e);
        }
    }

    private void handleBackToStartMenuCommand(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        lastShowVacancyLevel.remove(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Choose your title:");
        sendMessage.setReplyMarkup(getStartMenu());
        execute(sendMessage);
/*
        String text = update.getMessage().getText();
        System.out.println("Received text: " + text);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
//        sendMessage.setText("Your Message '" + text + "' was received.");
        sendMessage.setText("Welcome to vacancies bot! Please choose your title: ");
        sendMessage.setReplyMarkup(getStartMenu());
        execute(sendMessage);
    }

 */
    }

    private void handleBackToVacanciesCommand(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String vacancyLevel = lastShowVacancyLevel.get(chatId);
        showVacanciesOfLevel(vacancyLevel, update);
    }

    private void showVacancyDescription(String id, Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());

        // replaced on formatted text + additional code for this
        // sendMessage.setText(vacancyService.get(id).toString());

        VacancyDto vacancyDto = vacancyService.get(id);
        String vacancyInfo =
                """
                *Id:* %s
                *Title:* %s
                *Company:* %s
                *Salary:* %s
                *Link:* %s

                *Description:* 
                %s 

                *FullDescription:* 
                %s 

                *Link:* [%s](%s)
                """
                .formatted(
                        escapeMarkdownReservedChars(vacancyDto.getId()),
                        escapeMarkdownReservedChars(vacancyDto.getTitle()),
                        escapeMarkdownReservedChars(vacancyDto.getCompany()),
                        vacancyDto.getSalary().isBlank() ? "Not specified"
                                : escapeMarkdownReservedChars(vacancyDto.getSalary()),
                        escapeMarkdownReservedChars(vacancyDto.getLink()),
                        escapeMarkdownReservedChars(vacancyDto.getShortDescription()),
                        escapeMarkdownReservedChars(vacancyDto.getLongDescription()),
                        "Click here for more details", escapeMarkdownReservedChars(vacancyDto.getLink())
                );
        sendMessage.setText(vacancyInfo);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);

        sendMessage.setReplyMarkup(getBackToVacanciesMenu());
        execute(sendMessage);
    }

    private String escapeMarkdownReservedChars(String text) {
        final String RESERVED_CHARS = "-_*[]()~`>#+.!";
        String resultatString = text;
        if (text != null && text.length() > 0) {
            for (char symbol : RESERVED_CHARS.toCharArray()) {
                resultatString = resultatString.replace(String.valueOf(symbol), "\\" + symbol);
            }
        }
        return resultatString;
    }

    private ReplyKeyboard getBackToVacanciesMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton backToVacanciesButton = new InlineKeyboardButton();
        backToVacanciesButton.setText("Back to Vacancies");
        backToVacanciesButton.setCallbackData("backToVacancies");
        row.add(backToVacanciesButton);

        InlineKeyboardButton backToStartMenuButton = new InlineKeyboardButton();
        backToStartMenuButton.setText("Back to start menu");
        backToStartMenuButton.setCallbackData("backToStartMenu");
        row.add(backToStartMenuButton);

        InlineKeyboardButton getChatGptButton = new InlineKeyboardButton();
        getChatGptButton.setText("Get cover letter from ChatGpt");
        getChatGptButton.setUrl("https://chat.openai.com/");
        row.add(getChatGptButton);

        return new InlineKeyboardMarkup(List.of(row));
    }

    public void showVacanciesOfLevel(String vacancyLevel, Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        lastShowVacancyLevel.put(chatId, vacancyLevel);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy of "+ vacancyLevel);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getVacanciesOfLevelMenu(vacancyLevel));
        execute(sendMessage);
    }

    private ReplyKeyboard getVacanciesOfLevelMenu(String vacancyLevel) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<VacancyDto> vacancies = (vacancyLevel.equals("WithOutLevel"))
                ? vacancyService.getVacanciesWithOutLevel(vacancyLevels)
                : vacancyService.getVacanciesOfLevel(vacancyLevel);

        for (VacancyDto vacancy : vacancies) {
            InlineKeyboardButton vacancyButton = new InlineKeyboardButton();
            vacancyButton.setText(vacancy.getTitle());
            vacancyButton.setCallbackData("vacancyId=" + vacancy.getId());
            row.add(vacancyButton);
        }

        InlineKeyboardButton backToStartMenuButton = new InlineKeyboardButton();
        backToStartMenuButton.setText("Back to start menu");
        backToStartMenuButton.setCallbackData("backToStartMenu");
        row.add(backToStartMenuButton);
/*
        InlineKeyboardButton vacancy;

        vacancy = new InlineKeyboardButton();
        vacancy.setText(vacancyLevel + " Java developer at MateAcademy");
        vacancy.setCallbackData("vacancyId=" + vacancyLevel.charAt(0) + "001");
        row.add(vacancy);

        vacancy = new InlineKeyboardButton();
        vacancy.setText(vacancyLevel + " Dev at Google");
        vacancy.setCallbackData("vacancyId=" + vacancyLevel.charAt(0) + "002");
        row.add(vacancy);
*/
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }

/*
    public void showJuniorVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy: ");
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setReplyMarkup(getJuniorVacanciesMenu());
        execute(sendMessage);
    }
*/

/*
    private ReplyKeyboard getJuniorVacanciesMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();

        List<VacancyDto> vacancies = vacancyService.getJuniorVacancies();
        for (VacancyDto vacancy : vacancies) {
            InlineKeyboardButton vacancyButton = new InlineKeyboardButton();
            vacancyButton.setText(vacancy.getTitle());
            vacancyButton.setCallbackData("vacancyId=" + vacancy.getId());
            row.add(vacancyButton);
        }

        InlineKeyboardButton maVacancy = new InlineKeyboardButton();
        maVacancy.setText("Junior Java developer at MateAcademy");
        maVacancy.setCallbackData("vacancyId=j001");
        row.add(maVacancy);

        InlineKeyboardButton googleVacancy = new InlineKeyboardButton();
        googleVacancy.setText("Junior Dev at Google");
        googleVacancy.setCallbackData("vacancyId=j002");
        row.add(googleVacancy);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }

/*
    private ReplyKeyboard getMiddleVacanciesMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton maVacancy = new InlineKeyboardButton();
        maVacancy.setText("Middle Java developer at MateAcademy");
        maVacancy.setCallbackData("vacancyId=m001");
        row.add(maVacancy);

        InlineKeyboardButton googleVacancy = new InlineKeyboardButton();
        googleVacancy.setText("Middle Dev at Google");
        googleVacancy.setCallbackData("vacancyId=m002");
        row.add(googleVacancy);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }

    private ReplyKeyboard getSeniorVacanciesMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton maVacancy = new InlineKeyboardButton();
        maVacancy.setText("Senior Java developer at MateAcademy");
        maVacancy.setCallbackData("vacancyId=s001");
        row.add(maVacancy);

        InlineKeyboardButton googleVacancy = new InlineKeyboardButton();
        googleVacancy.setText("Senior Dev at Google");
        googleVacancy.setCallbackData("vacancyId=s002");
        row.add(googleVacancy);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }
*/

    private void handleStartCommand (Update update) throws TelegramApiException {
        String text = update.getMessage().getText();
        System.out.println("Received text: " + text);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
//        sendMessage.setText("Your Message '" + text + "' was received.");
        sendMessage.setText("Welcome to vacancies bot!\nPlease choose your title: ");
        sendMessage.setReplyMarkup(getStartMenu());
        execute(sendMessage);
    }

    private ReplyKeyboard getStartMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (String vacancyLevel : vacancyLevels) {
            InlineKeyboardButton expLevelButton = new InlineKeyboardButton();
            expLevelButton.setText(vacancyLevel);
            expLevelButton.setCallbackData("showVacanciesOf" + vacancyLevel);
            row.add(expLevelButton);
        }

        InlineKeyboardButton expLevelButton = new InlineKeyboardButton();
        expLevelButton.setText("With out of level");
        expLevelButton.setCallbackData("showVacanciesOfWithOutLevel");
        row.add(expLevelButton);

/*
        InlineKeyboardButton junior = new InlineKeyboardButton();
        junior.setText("Junior");
        junior.setCallbackData("showVacanciesOfJunior");
        row.add(junior);
*/
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }

    @Override
    public String getBotUsername() {
        return "KANtavr vacancies bot";
    }

}
