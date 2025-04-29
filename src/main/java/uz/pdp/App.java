package uz.pdp;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.impl.UpdatesHandler;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        TelegramBot bot=new TelegramBot("7951519647:AAE-iO4IETn8jUhgWoT85h1syxlF9MLmTQ0");
        UpdateHandler updateHandler=new UpdateHandler(bot);
        bot.setUpdatesListener(updates->{
            updates.forEach(updateHandler::handle);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

    }
}
