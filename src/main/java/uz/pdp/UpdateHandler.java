package uz.pdp;

import com.github.javafaker.Faker;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static uz.pdp.UserStates.*;

public class UpdateHandler {
    private TelegramBot bot;
    private static final ConcurrentHashMap<Long,UserStates> userStates=new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Integer> lastMessageIds = new ConcurrentHashMap<>();
    private static final List<Book> books = new ArrayList<>();
    private static final List<SaleHistory> sales = new ArrayList<>();
    private final Faker faker = new Faker();
    private final Long ADMIN_ID = 123456789L;

    public UpdateHandler(TelegramBot bot) {
        this.bot = bot;
    }

    public void handle(Update update){
        if(update.message()!=null){
            Long chatId=update.message().from().id();
            String text=update.message().text();

            if(!userStates.containsKey(chatId)){
                if (chatId.equals(ADMIN_ID)) {
                    userStates.put(chatId,ADMIN_MENU);
                    sendAdminMenu(chatId);
                }else{
                    userStates.put(chatId,USER_MENU);
                    sendAdminMenu(chatId);
                }
            }
            switch (userStates.get(chatId)){
                case ADMIN_MENU -> handleAdminMenu(chatId, text);
                case ADD_BOOK_NAME -> handleAddBookName(chatId, text);
                case ADD_BOOK_AUTHOR -> handleAddBookAuthor(chatId, text);
                case ADD_BOOK_PRICE -> handleAddBookPrice(chatId, text);
                case ADD_BOOK_PHOTO -> handleAddBookPhoto(chatId, text);
                case ADD_BOOK_QUANTITY -> handleAddBookQuantity(chatId, text);
                case USER_MENU -> handleUserMenu(chatId, text);
                case USER_SEARCH_BOOK -> searchBook(chatId, text);
                case USER_BUY_BOOK -> buyBook(chatId, text);
            }

        }else if(update.callbackQuery()!=null){


        }
    }

    private void buyBook(Long chatId, String text) {
        for (Book book : books) {
            String bookTitle = "";
            if (book.getTitle().equalsIgnoreCase(bookTitle)) {
                if (book.getQuantity() > 0) {
                    book.setQuantity(book.getQuantity() - 1);
                    sales.add(new SaleHistory(chatId, faker.name().fullName(), book.getTitle(), 1, LocalDateTime.now()));
                    bot.execute(new SendMessage(chatId, "✅ Sotib oldingiz: " + book.getTitle()));
                } else {
                    bot.execute(new SendMessage(chatId, "❌ Bu kitob tugagan."));
                }
                sendUserMenu(chatId);
                return;
            }
        }
        bot.execute(new SendMessage(chatId, "❌ Kitob topilmadi."));
        sendUserMenu(chatId);
    }

    private void searchBook(Long chatId, String text) {
        StringBuilder sb = new StringBuilder("🔎 Qidiruv natijalari:\n\n");
        for (Book book : books) {
            String query = "";
            if (book.getTitle().toLowerCase().contains(query.toLowerCase())) {
                sb.append("• ").append(book.getTitle())
                        .append(" - ").append(book.getAuthor())
                        .append(" (").append(book.getPrice()).append("$)\n");
            }
        }
        bot.execute(new SendMessage(chatId, sb.toString()));
        sendUserMenu(chatId);
    }

    private void sendUserMenu(Long chatId) {
        bot.execute(new SendMessage(chatId, "👤 Foydalanuvchi Panel:")
                .replyMarkup(new ReplyKeyboardMarkup(
                        new KeyboardButton[]{
                                new KeyboardButton("📚 Kitoblar ro'yxati"),
                                new KeyboardButton("🔎 Kitob qidirish"),
                                new KeyboardButton("🛒 Sotib olish")
                        }).resizeKeyboard(true).oneTimeKeyboard(true)
                ));
    }

    private void handleUserMenu(Long chatId, String text) {
        switch (text) {
            case "📚 Kitoblar ro'yxati" -> {
                showAllBooksUser(chatId);
                sendUserMenu(chatId);
            }
            case "🔎 Kitob qidirish" -> {
                bot.execute(new SendMessage(chatId, "Qidirilayotgan kitob nomini yozing:"));
                userStates.put(chatId, USER_SEARCH_BOOK);
            }
            case "🛒 Sotib olish" -> {
                showAllBooksUser(chatId);
                bot.execute(new SendMessage(chatId, "Sotib olmoqchi bo'lgan kitob nomini yozing:"));
                userStates.put(chatId, USER_BUY_BOOK);
            }
        } 
    }

    private void showAllBooksUser(Long chatId) {
        if (books.isEmpty()) {
            bot.execute(new SendMessage(chatId, "📚 Hali kitoblar yo'q."));
            return;
        }
        StringBuilder sb = new StringBuilder("📚 Mavjud kitoblar:\n\n");
        for (Book book : books) {
            sb.append("• ").append(book.getTitle())
                    .append(" - ").append(book.getAuthor())
                    .append(" (").append(book.getPrice()).append("$)\n");
        }
        bot.execute(new SendMessage(chatId, sb.toString()));
    }

    private void handleAddBookQuantity(Long chatId, String text) {
        tempBook.setQuantity(Integer.parseInt(text));
        tempBook=new Book();
        bot.execute(new SendMessage(chatId, "✅ Kitob qo'shildi!"));
        sendAdminMenu(chatId);
        userStates.put(chatId, ADMIN_MENU);
    }

    private void handleAddBookPrice(Long chatId, String text) {
        tempBook.setPrice(Double.parseDouble(text));
        bot.execute(new SendMessage(chatId, "Rasm linkini kiriting:"));
        userStates.put(chatId, ADD_BOOK_PHOTO);
    }

    private void handleAddBookPhoto(Long chatId, String text) {
        tempBook.setPhotoUrl(text);
        bot.execute(new SendMessage(chatId, "Nechta mavjudligini kiriting:"));
        userStates.put(chatId, ADD_BOOK_PHOTO);
    }

    private void handleAddBookAuthor(Long chatId, String text) {
        tempBook.setAuthor(text);
        bot.execute(new SendMessage(chatId,"Narxini kiriting: "));
        userStates.put(chatId,ADD_BOOK_PRICE);
    }
     private Book tempBook;
    private void handleAddBookName(Long chatId, String text) {
        tempBook=new Book();
        tempBook.setTitle(text);
        bot.execute(new SendMessage(chatId,"Muallifni kiriting: "));
        userStates.put(chatId,ADD_BOOK_AUTHOR);

    }

    private void handleAdminMenu(Long chatId, String text) {
        switch (text){
            case "➕ Kitob qo'shish"->{
                bot.execute(new SendMessage(chatId,"Kitob nomini kiriting: "));
                userStates.put(chatId,ADD_BOOK_NAME);
            }
            case "📚 Kitoblar ro'yxati"->{ showAllBooksAdmin(chatId);}
            case "📈 Sotuv Tarixi"->{ showSaleHistory(chatId);}
        }
    }



    private void showSaleHistory(Long chatId) {
        if (sales.isEmpty()) {
            bot.execute(new SendMessage(chatId, "🛒 Sotuvlar hali bo'lmagan."));
            return;
        }
        StringBuilder sb = new StringBuilder("📈 Sotuvlar tarixi:\n\n");
        for (SaleHistory sale : sales) {
            sb.append("👤 ").append(sale.getUserName()).append("\n")
                    .append("📚 ").append(sale.getBookTitle()).append("\n")
                    .append("🕒 ").append(sale.getPurchasedAt()).append("\n\n");
        }
        bot.execute(new SendMessage(chatId, sb.toString()));
    }

    private void showAllBooksAdmin(Long chatId) {
        if (books.isEmpty()) {
            bot.execute(new SendMessage(chatId, "📚 Hali kitoblar yo'q."));
            return;
        }
        StringBuilder sb = new StringBuilder("📚 Kitoblar:\n\n");
        for (Book book : books) {
            sb.append("• ").append(book.getTitle()).append(" - ").append(book.getAuthor())
                    .append(" (").append(book.getQuantity()).append(" dona)\n");
        }
        bot.execute(new SendMessage(chatId, sb.toString()));
    }

    private void sendAdminMenu(Long chatId) {
        bot.execute(new SendMessage(chatId,"\uD83D\uDD27  Admin panel")
                .replyMarkup(new ReplyKeyboardMarkup(
                        new KeyboardButton[]{
                                new KeyboardButton("➕ Kitob qo'shish"),
                                new KeyboardButton("📚 Kitoblar ro'yxati"),
                                new KeyboardButton("📈 Sotuv Tarixi")
                        }).resizeKeyboard(true).oneTimeKeyboard(true)
                ));

        so

    }
}
