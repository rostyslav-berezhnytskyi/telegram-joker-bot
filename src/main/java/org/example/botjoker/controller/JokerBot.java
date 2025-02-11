package org.example.botjoker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.example.botjoker.config.BotConfig;
import org.example.botjoker.model.Joke;
import org.example.botjoker.repository.JokeRepository;
import org.example.botjoker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class JokerBot extends TelegramLongPollingBot {
    private final UserRepository userRepository;
    private final JokeRepository jokeRepository;
    private final BotConfig config;
    private final Random random = new Random();

    static private final int MAX_JOKE_ID_PLUS_ONE = 3774;

    static final String HELP_TEXT = "This bot is created to send a random joke from the database each time you request it.\n\n" +
            "You can execute commands from the main menu on the left or by typing commands manually\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /joke to get a random joke\n\n" +
            "Type /settings to list available settings to configure\n\n" +
            "Type /help to see this message again\n";

    @Autowired
    public JokerBot(UserRepository userRepository, JokeRepository jokeRepository, BotConfig config) {
        this.userRepository = userRepository;
        this.jokeRepository = jokeRepository;
        this.config = config;
        createListOfCommands();
//        downloadDataFromFile();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" -> showStart(chatId, update.getMessage().getChat().getUserName());
                case "/help" -> sendMessage(chatId, HELP_TEXT);
                case "/joke" -> {
                    long randomId = random.nextInt(1, MAX_JOKE_ID_PLUS_ONE);
                    Optional<Joke> randomJoke = jokeRepository.findById(randomId);

                    randomJoke.ifPresent(j -> sendMessage(chatId, j.getBody()));
                }
                default -> commandNotFound(chatId);
            }
        }
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        send(message);
    }

    public void send(SendMessage message) {
        try{
            this.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred " + e.getMessage());
        }
    }

    public void showStart(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode(
                "Hi, " + name + "!, :smile:" + " Nice to meet you!");
        sendMessage(chatId, answer);
    }

    public void commandNotFound(long chatId) {
        String answer = EmojiParser.parseToUnicode(
                "Command not recognized, please verify and ");
        sendMessage(chatId, answer);
    }

    public void createListOfCommands() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/joke", "get a random joke"));
        listOfCommands.add(new BotCommand("/help", "info how to use this bot"));
        listOfCommands.add(new BotCommand("/settings", "set your preferences"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    public void downloadDataFromFile() {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        try {
            List<Joke> jokeList = objectMapper.readValue(new File("db/stupidstuff.json"), typeFactory.constructCollectionType(List.class, Joke.class));
            jokeRepository.saveAll(jokeList);
        } catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }

    }

    @Override
    public String getBotUsername() {
        return config.getBotUserName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
