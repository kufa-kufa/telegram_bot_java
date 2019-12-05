import com.mysql.fabric.jdbc.FabricMySQLDriver;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;


import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class KufaEduBot extends TelegramLongPollingBot {

        private static final String URL = "jdbc:mysql://localhost:3306/transportation_fine";
        private static final String USERNAME = "root";
        private static final String PASSWORD = "";
        Connection connection = null;
        Driver driver;
        public void onUpdateReceived(Update update) {
            String PIN=null;

//            System.out.println(update.getMessage().getText());
//            System.out.println(update.getMessage().getFrom().getFirstName() );
            Message message = update.getMessage();
            String command=message.getText();

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            if(command.equals("/start")){
               String welcomeMessage = "Проверка штрафов\n" +
                       "Услуга для предоставления гражданам информации об административных правонарушениях," +
                       "вынесенных на основании данных, полученных со специальных технических средств автоматической " +
                       "фиксации нарушений." +
                       "Для проверки шрафов"+
                       "Введите ПИН, 14 значный идентификационный номер, который указан на паспорте.";
                sendMessage.setText(welcomeMessage);
            }
            else {
                PIN = command;
                try {
                    sendMessage.setText(getFines(PIN));
                }
                catch (SQLException x) {
                    System.out.println(x.getMessage());
                }
            }

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        public String getBotUsername() {
            return "ExampleShtrafBot";
        }
        public String getBotToken() {
            return "998405930:AAHc3IxWwN0W52QNyMGbyACpRM0H2rZtuL0";
        }
        private String getFines(String pin) throws SQLException {
            String fines = null;
            //https://www.youtube.com/watch?v=jRV8E6IIOHs
            try {
                driver = new FabricMySQLDriver();
                DriverManager.registerDriver(driver);
            }
            catch (SQLException e) {
                System.out.println("Error Driver can not created");
            }
            try {
                Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                Statement statement = connection.createStatement();
               // preparedStatement.setString(1, pin);
                String sql = "SELECT c.first_name, c.last_name, c.pin, f.car_number, f.fee, f.description FROM fines f LEFT JOIN citizens c ON c.id = f.citizen_id WHERE c.pin='"+pin+"'";
                ResultSet resultSet = statement.executeQuery(sql);

                while (resultSet.next()) {
                    if(resultSet.isFirst()) {
                        fines = "Имя нарушителя: " + resultSet.getString("first_name") + "\n";
                    }
                    fines += "Номер машины: " + resultSet.getString("car_number") + "\n" +
                            "Размер штрафа: " + resultSet.getString("fee") + "\n" +
                            "Нарушение: " + resultSet.getString("description") + "\n";
                }
                if(fines==null) {
                    fines = "Нет нарушений";
                }
                statement.close();
                connection.close();
            }
            catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }


            return fines;
         }


    }
