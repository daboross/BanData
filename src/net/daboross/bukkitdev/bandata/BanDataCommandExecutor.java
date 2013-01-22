package net.daboross.bukkitdev.bandata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import net.daboross.bukkitdev.playerdata.ColorL;
import net.daboross.bukkitdev.playerdata.Data;
import net.daboross.bukkitdev.playerdata.PlayerData;
import net.daboross.bukkitdev.playerdata.PlayerDataHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 *
 * @author daboross
 */
public class BanDataCommandExecutor implements CommandExecutor {

    private final Map<String, String> aliasMap = new HashMap<>();
    private final Map<String, Boolean> isConsoleMap = new HashMap<>();
    private final Map<String, String> helpList = new HashMap<>();
    private final Map<String, String> permMap = new HashMap<>();
    private PlayerData playerDataMain;
    private PlayerDataHandler pDataH;
    private BanData banDataMain;

    /**
     *
     */
    protected BanDataCommandExecutor(BanData bd) {
        this.playerDataMain = bd.getPlayerData();
        this.banDataMain = bd;
        this.pDataH = playerDataMain.getHandler();
        aliasMap.put("?", "?");
        aliasMap.put("help", "?");
        isConsoleMap.put("?", true);
        permMap.put("?", "bandata.help");
        helpList.put("?", "This Command Views This Page");

        aliasMap.put("ban", "ban");
        isConsoleMap.put("ban", true);
        permMap.put("ban", "bandata.ban");
        helpList.put("ban", ColorL.ARGS + "<Player> <Reason>" + ColorL.HELP + " Bans A Player With PEX and records Info.");

        aliasMap.put("viewban", "viewban");
        aliasMap.put("vb", "viewban");
        aliasMap.put("i", "viewban");
        isConsoleMap.put("viewban", true);
        permMap.put("viewban", "bandata.viewinfo");
        helpList.put("viewban", ColorL.ARGS + "<Player> <Ban Number> (or just <Player> if you want to see how many bans there are)" + ColorL.HELP + " Views Ban Info On a Player");

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("bd")) {
            if (args.length < 1) {
                sender.sendMessage(ColorL.MAIN + "This is a base command, Please Use a sub command after it.");
                sender.sendMessage(ColorL.MAIN + "To see all possible sub commands, type " + ColorL.CMD + "/" + cmd.getName() + ColorL.SUBCMD + " ?");
                return true;
            }
            String commandName;
            if (aliasMap.containsKey(args[0].toLowerCase())) {
                commandName = aliasMap.get(args[0].toLowerCase());
                Bukkit.getServer().getLogger().log(Level.INFO, (sender.getName() + " used " + commandName));
            } else {
                sender.sendMessage(ColorL.MAIN + "The SubCommand: " + ColorL.CMD + args[0] + ColorL.MAIN + " Does not exist.");
                sender.sendMessage(ColorL.MAIN + "To see all possible sub commands, type " + ColorL.CMD + "/" + cmd.getName() + ColorL.SUBCMD + " ?");
                return true;
            }
            if (sender instanceof Player) {
                if (!sender.hasPermission(permMap.get(commandName))) {
                    sender.sendMessage(ColorL.NOPERM + "You don't have permission to do this command!");
                    return true;
                }
            }
            boolean isConsole;
            if (isConsoleMap.containsKey(commandName)) {
                isConsole = isConsoleMap.get(commandName);
            } else {
                isConsole = false;
            }
            if (!(sender instanceof Player)) {
                if (!isConsole) {
                    sender.sendMessage(ColorL.NOPERM + "This command must be run by a player");
                    return true;
                }
            }
            if (commandName.equalsIgnoreCase("?")) {
                runHelpCommand(sender, cmd, getSubArray(args));
            } else if (commandName.equalsIgnoreCase("ban")) {
                runBanCommand(sender, cmd, args[0], getSubArray(args));
            } else if (commandName.equalsIgnoreCase("viewban")) {
                runViewBanCommand(sender, cmd, args[0], getSubArray(args));
            }
            return true;
        }
        return false;
    }

    private String[] getSubArray(String[] array) {
        if (array.length > 1) {
            return Arrays.asList(array).subList(1, array.length).toArray(new String[0]);
        } else {
            return new String[0];
        }
    }

    private void runHelpCommand(CommandSender sender, Command cmd, String[] args) {
        sender.sendMessage(ColorL.MAIN + "List Of Possible Sub Commands:");
        for (String str : aliasMap.keySet()) {
            sender.sendMessage(getHelpMessage(str, cmd.getLabel()));
        }
    }

    private void runBanCommand(CommandSender sender, Command cmd, String aliasLabel, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorL.ILLEGALARGUMENT + "Please Specify a Player Name and a Ban Reason!");
            sender.sendMessage(getHelpMessage(aliasLabel, cmd.getLabel()));
            return;
        }

        if (!pDataH.doesPlayerExists(args[0])) {
            sender.sendMessage(ColorL.MAIN + "No Player whoes full name matches " + ColorL.NAME + args[0] + ColorL.MAIN + " was found.");
            sender.sendMessage(ColorL.MAIN + "Do To The Nature of this command, please specify the full username of a player.");
            String fullUserName = pDataH.getFullUsername(args[0]);
            if (fullUserName != null) {
                sender.sendMessage(ColorL.MAIN + "Did you mean " + ColorL.NAME + fullUserName + ColorL.MAIN + "?");
            }
            return;
        }
        String playerToBanUserName = pDataH.getFullUsername(args[0]);
        OfflinePlayer playerToBan = Bukkit.getOfflinePlayer(playerToBanUserName);
        if (!playerToBan.hasPlayedBefore()) {
            banDataMain.getLogger().log(Level.SEVERE, "Player Username Passed By Player Data Hasn't Played Before!!!");
            sender.sendMessage(ColorL.ERROR + "Error!");
            return;
        }
        String reason = "";
        for (int i = 1; i < args.length; i++) {
            reason += args[i];
        }
        sender.sendMessage(ColorL.MAIN + "Banning " + ColorL.NAME + playerToBanUserName + ColorL.MAIN + " for " + ColorL.NUMBER + reason);
        PermissionUser permPlayer = PermissionsEx.getUser(playerToBanUserName);
        PermissionGroup[] oldGroups = permPlayer.getGroups();
        String oldGroup = null;
        for (PermissionGroup pg : oldGroups) {
            if (pg.has("basic")) {
                oldGroup = pg.toString();
            }
        }
        if (oldGroup == null) {
            oldGroup = "Basic";
        }
        permPlayer.setGroups(new String[]{"Banned"});
        Ban ban;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location loc = player.getLocation();
            ban = new Ban(reason, oldGroup, (long) loc.getX(), (long) loc.getY(), (long) loc.getZ(), loc.getWorld().getName(), System.currentTimeMillis());
        } else {
            ban = new Ban(reason, oldGroup, System.currentTimeMillis());

        }
        Data rawData = pDataH.getCustomData(playerToBanUserName, "bandata");
        BData banData;
        if (rawData == null) {
            Ban[] banList = new Ban[]{ban};
            banData = new BData(banList);
        } else {
            banData = DataParser.parseFromlist(rawData.getData());
            banData.addBan(ban);
        }
        String[] newRawBanData = DataParser.parseToList(banData);
        Data banDataToSet = new Data("bandata", newRawBanData);
        pDataH.addCustomData(playerToBanUserName, banDataToSet);

    }

    private void runViewBanCommand(CommandSender sender, Command cmd, String aliasLabel, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ColorL.ILLEGALARGUMENT + "Please Specify a Player Name to get info from!");
            sender.sendMessage(getHelpMessage(aliasLabel, cmd.getLabel()));
            return;
        }
        if (args.length > 2) {
            sender.sendMessage(ColorL.ILLEGALARGUMENT + "Please Only Use One Word and a number after " + ColorL.SUBCMD + aliasLabel);
            sender.sendMessage(getHelpMessage(aliasLabel, cmd.getLabel()));
            return;
        }
        String playerUserName = pDataH.getFullUsername(args[0]);
        if (playerUserName == null) {
            sender.sendMessage(ColorL.MAIN + "The Player " + ColorL.NAME + args[0] + ColorL.MAIN + " was not found.");
            return;
        }
        Data rawData = pDataH.getCustomData(playerUserName, "bandata");
        if (rawData == null) {
            sender.sendMessage(ColorL.MAIN + "Found no ban data for Player " + ColorL.NAME + playerUserName + ColorL.MAIN + ".");
        }
        BData banData = DataParser.parseFromlist(rawData.getData());
        if (args.length > 1) {
            int number;
            try {
                number = Integer.valueOf(args[1]);
            } catch (Exception e) {
                sender.sendMessage(ColorL.ERROR_ARGS + args[1] + ColorL.ERROR + " is not a number.");
                sender.sendMessage(getHelpMessage(aliasLabel, cmd.getLabel()));
                return;
            }
            sender.sendMessage(InfoParser.getInstance().banInfo(rawData, banData, number));
        } else {
            sender.sendMessage(InfoParser.getInstance().shortInfo(rawData));
            sender.sendMessage(ColorL.MAIN + "Type " + ColorL.CMD + "/" + cmd.getLabel() + " " + ColorL.SUBCMD + aliasLabel + " " + ColorL.ARGS + args[0] + "{ 0 - " + banData.getBans().length + " }" + ColorL.MAIN + " for more info on a ban");
        }
    }

    private String getHelpMessage(String alias, String baseCommand) {
        String str = aliasMap.get(alias);
        return (ColorL.CMD + "/" + baseCommand + ColorL.SUBCMD + " " + alias + ColorL.HELP + " " + helpList.get(aliasMap.get(str)));
    }
}