package utility;

import java.io.BufferedInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static java.lang.System.in;
import static utility.Readline.readLine;

public class CommandManager {
    private static final HashSet<String> dataCommand = new HashSet<>(Arrays.asList("remove_key", "remove_all_by_government"));
    private static final HashSet<String> creatorCommand = new HashSet<>(Arrays.asList("remove_lower", "insert", "update", "replace_if_greater"));
    private static final HashSet<String> humanCommand = new HashSet<>(Collections.singletonList("filter_less_than_governor"));
    private static final HashSet<String> bareCommand = new HashSet<>(Arrays.asList("help", "exit", "show", "clear", "print_descending", "history", "info"));

    public static Request execute(String commandName, String data) {
        Request request = null;
        if (!commandName.equals("")){
            if (commandName.equals("exit")){
                Client.closeConnection();
                System.exit(0);
            } else if (commandName.equals("login")){
                String[] logpass = new String[2];
                System.out.print("введите логин:");
                logpass[0] = readLine(new BufferedInputStream(in));
                System.out.print("введите пароль:");
                logpass[1] = readLine(new BufferedInputStream(in));
                request = new Request("login", logpass);
            }
            else {
                if (dataCommand.contains(commandName)) {
                    request = new Request(commandName);
                    request.setDataArgument(data);
                } else if (creatorCommand.contains(commandName)) {
                    request = new Request(commandName);
                    try {
                        Long.parseLong(data);
                        request.setCreatorArgument(CollectingDataForCityCreator.execute(data));
                    } catch (NumberFormatException e) {
                        request.setCreatorArgument(CollectingDataForCityCreator.execute());
                    }
                } else if (humanCommand.contains(commandName)) {
                    request = new Request(commandName);
                    request.setHumanArgument(CollectingDataForGovernor.execute());
                } else if (bareCommand.contains(commandName)) {
                    request = new Request(commandName);
                } else System.out.println("Такой команды нет, для списка команд напишите help");
            }
        }

        return request;
    }
}
