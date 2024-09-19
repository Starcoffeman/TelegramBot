package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeworkBot extends TelegramLongPollingBot {

    private final Map<String, List<String>> tasksDescriptions = new HashMap<>(); // Условия заданий
    private final Map<String, List<String>> tasksFiles = new HashMap<>(); // Выполненные задания

    public HomeworkBot() {
        List<String> tasksDescriptionsForTeacher1 = new ArrayList<>();
        tasksDescriptionsForTeacher1.add("src/main/resources/prepods/gorlanov/first/task/ПР1.zip");
        tasksDescriptionsForTeacher1.add("src/main/resources/prepods/gorlanov/second/task/ПР2.zip");
        tasksDescriptionsForTeacher1.add("src/main/resources/prepods/gorlanov/third/task/ПР3.zip");
        tasksDescriptionsForTeacher1.add("src/main/resources/prepods/gorlanov/sql/task/ПР3.dock");

        tasksDescriptions.put("Горланов", tasksDescriptionsForTeacher1);
//
//
//        List<String> tasksDescriptionsForTeacher2 = new ArrayList<>();
//        tasksDescriptionsForTeacher1.add("src/main/resources/prepods/malinin/first/task/ПР1.zip");
//        tasksDescriptionsForTeacher1.add("src/main/resources/prepods/malinin/second/task/ПР2.zip");
//        tasksDescriptionsForTeacher1.add("src/main/resources/prepods/malinin/third/task/ПР3.zip");
//        tasksDescriptionsForTeacher1.add("src/main/resources/prepods/malinin/third/task/ПР4.zip");
//        tasksDescriptions.put("Малинин", tasksDescriptionsForTeacher2);

        // Обновление списка выполненных заданий
        List<String> tasksFilesForTeacher1 = new ArrayList<>();
        tasksFilesForTeacher1.add("src/main/resources/prepods/gorlanov/first/done/(ВР1)Практическая работа №1,Фамилия_3ИСИП-122.zip"); // Выполненное задание ПР1
        tasksFilesForTeacher1.add("src/main/resources/prepods/gorlanov/second/done/(ВР2)Практическая_работа_№1,Фамилия_3ИСИП_122.zip"); // Выполненное задание ПР2
//        tasksFilesForTeacher1.add("src/main/java/org/example/prepods/gorlanov/sql/done/ПР.zip"); // Выполненное задание SQL
        tasksFiles.put("Горланов", tasksFilesForTeacher1);
    }

    @Override
    public String getBotUsername() {
        return "HelperCoffeebot";
    }

    @Override
    public String getBotToken() {
        return "7355762793:AAHFZjtJtGr1yU_HngIisju5QFaEJvKks5Q";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start") || messageText.equals("Start")) {
                sayHello(chatId);
                sendMainMenu(chatId);
            } else if (messageText.equals("Info")) {
                sendGreeting(chatId);
            } else if (messageText.equals("Преподаватели")) {
                sendTeachersMenu(chatId);
            } else if (messageText.equals("Поддержка")) {
                sendHelp(chatId);
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (tasksDescriptions.containsKey(callbackData)) {
                sendTasksMenu(chatId, callbackData);
            } else {
                // Разделяем callbackData на действие и номер задания
                String[] parts = callbackData.split(":");
                String action = parts[0];  // "condition" или "done"
                String taskId = parts[1];  // Номер задания (например, "1" для ПР1)

                if (action.equals("condition")) {
                    if(taskId.equals("SQL")){
                        sendTaskFile(chatId, taskId, "src/main/resources/prepods/gorlanov/sql/task/SQL.zip");
                    }
                    sendTaskFile(chatId, taskId, tasksDescriptions.get("Горланов").get(Integer.parseInt(taskId) - 1));
                } else if (action.equals("done")) {
                    if(taskId.equals("SQL")){
                        sendTaskFilesFromDirectory(chatId, "SQL", "src/main/resources/prepods/gorlanov/sql/done/SQL.zip");
                    }

                    if (taskId.equals("1")) {
                        sendTaskFilesFromDirectory(chatId, "ПР1", "src/main/resources/prepods/gorlanov/first/done");
                    } else if (taskId.equals("2")) {
                        sendTaskFilesFromDirectory(chatId, "ПР2", "src/main/resources/prepods/gorlanov/second/done");
                    } else if (taskId.equals("3")) {
                        sendTaskFilesFromDirectory(chatId, "ПР3", "src/main/resources/prepods/gorlanov/third/done");
                    } else {
                        sendTaskFile(chatId, taskId, tasksFiles.get("Горланов").get(Integer.parseInt(taskId) - 1));
                    }
                }
            }
        }
    }


    private void sendMainMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите действие:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("Info");
        row.add("Преподаватели");
        row.add("Поддержка");
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendTeachersMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите преподавателя:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        // Добавляем кнопки с преподавателями
        for (String teacher : tasksDescriptions.keySet()) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(teacher);
            button.setCallbackData(teacher);
            row.add(button);
            buttons.add(row);
        }

        markup.setKeyboard(buttons);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void sendTasksMenu(long chatId, String teacher) {
//        SendMessage message = new SendMessage();
//        message.setChatId(chatId);
//        message.setText("Выберите задание от " + teacher + ":");
//
//        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
//
//        List<String> tasks = tasksDescriptions.get(teacher);
//
//        for (int i = 0; i < tasks.size(); i++) {
//            String taskName = "ПР" + (i + 1);
//
//            InlineKeyboardButton conditionButton = new InlineKeyboardButton();
//            conditionButton.setText(taskName + " - Условие");
//            conditionButton.setCallbackData("condition:" + (i + 1));  // Добавляем callbackData с меткой "condition" и номером задания
//
//            InlineKeyboardButton doneButton = new InlineKeyboardButton();
//            doneButton.setText(taskName + " - Готовое");
//            doneButton.setCallbackData("done:" + (i + 1));  // Добавляем callbackData с меткой "done" и номером задания
//
//            List<InlineKeyboardButton> row = new ArrayList<>();
//            row.add(conditionButton);
//            row.add(doneButton);
//
//            buttons.add(row);
//        }
//
//        markup.setKeyboard(buttons);
//        message.setReplyMarkup(markup);
//
//        try {
//            execute(message);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void sendTasksMenu(long chatId, String teacher) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите задание от " + teacher + ":");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<String> tasks = tasksDescriptions.get(teacher);

        for (int i = 0; i < tasks.size() - 1; i++) {
            String taskName = "ПР" + (i + 1);

            InlineKeyboardButton conditionButton = new InlineKeyboardButton();
            conditionButton.setText(taskName + " - Условие");
            conditionButton.setCallbackData("condition:" + (i + 1));  // Добавляем callbackData с меткой "condition" и номером задания

            InlineKeyboardButton doneButton = new InlineKeyboardButton();
            doneButton.setText(taskName + " - Готовое");
            doneButton.setCallbackData("done:" + (i + 1));  // Добавляем callbackData с меткой "done" и номером задания

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(conditionButton);
            row.add(doneButton);
            buttons.add(row);
        }


        InlineKeyboardButton conditionButton = new InlineKeyboardButton();
        conditionButton.setText("SQL - Условие");
        conditionButton.setCallbackData("condition:" + "SQL");  // Добавляем callbackData с меткой "condition" и номером задания

        InlineKeyboardButton doneButton = new InlineKeyboardButton();
        doneButton.setText("SQL - Готовое");
        doneButton.setCallbackData("done:" + "SQL");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(conditionButton);
        row.add(doneButton);
        buttons.add(row);

        markup.setKeyboard(buttons);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendTaskFile(long chatId, String task, String pathFile) {
        SendDocument document = new SendDocument();
        document.setChatId(chatId);
        document.setCaption("Файлы по заданию: " + task);

        File file = new File(pathFile);
        document.setDocument(new InputFile(file));

        try {
            execute(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new HomeworkBot());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendGreeting(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("В этом боте храняться условия и выполненные задания по предметам! " +
                "Выполненные работы можно использовать,как шаблон или просто отправить. Если в названии архива будет написано ,например, «(ВР1)» то очевидно надо убирать" + "\n\n" +
                "⚠️ ВНИМАНИЕ ⚠️" + "\n" +
                "ГЛАВНОЕ — ПОМЕНЯТЬ НАЗВАНИЕ ФАЙЛА И ВНУТРИ ПОМЕНЯТЬ ФИО.");

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sorry(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Не удалось найти файлы для задания или не выполнена!");

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendHelp(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Если вы считаете, что работа сделана неверно и хотите чтобы это исправили, то напишите в " +
                "лс. " + "\n\n" + "А если вы хотите пожаловаться что за работу, которую вы взяли из этого бота, " +
                "вы получили 4(при условии что работа сделана правильно), а вы хотели 5. То кроме цитирования мне ничего не остаётся: " + "\n" + "«Чё?? Закибербулили тебя? Да? Ну я не знаю, выключи компьютер хз, иди н***й от суда давай..»");

        try {
            execute(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sayHello(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("В этом боте храняться условия и выполненные задания по предметам! " +
                "Выполненные работы можно использовать,как шаблон или просто отправить. Преподавателей буду добавлять постепенно" + "\n" + "Привет всем из 3ИСИП-822!");
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendTaskFilesFromDirectory(long chatId, String task, String directoryPath) {
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles(); // Получаем все файлы из директории

        if (listOfFiles != null && listOfFiles.length > 0) {
            SendMessage infoMessage = new SendMessage();
            infoMessage.setChatId(chatId);
            infoMessage.setText("Файлы по заданию: " + task);

            try {
                execute(infoMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (File file : listOfFiles) {
                if (file.isFile()) {  // Проверяем, что это файл, а не папка
                    SendDocument document = new SendDocument();
                    document.setChatId(chatId);
                    document.setDocument(new InputFile(file));

                    try {
                        execute(document);  // Отправляем файл пользователю
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Не удалось найти файлы для задания или не выполнена: " + task);
            try {
                execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

