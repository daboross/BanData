package net.daboross.bukkitdev.bandata.commandreactors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.daboross.bukkitdev.bandata.BData;
import net.daboross.bukkitdev.bandata.Ban;
import net.daboross.bukkitdev.bandata.DataParser;
import net.daboross.bukkitdev.playerdata.libraries.commandexecutorbase.ColorList;
import net.daboross.bukkitdev.playerdata.libraries.commandexecutorbase.SubCommand;
import net.daboross.bukkitdev.playerdata.libraries.commandexecutorbase.SubCommandHandler;
import net.daboross.bukkitdev.playerdata.Data;
import net.daboross.bukkitdev.playerdata.PData;
import net.daboross.bukkitdev.playerdata.PlayerData;
import net.daboross.bukkitdev.playerdata.PlayerDataHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author daboross
 */
public class UnBanCommandReactor implements SubCommandHandler {

    private final PlayerDataHandler playerDataHandler;

    public UnBanCommandReactor(PlayerDataHandler playerDataHandler) {
        this.playerDataHandler = playerDataHandler;
    }

    public void runCommand(CommandSender sender, Command baseCommand, String baseCommandLabel, SubCommand subCommand, String subCommandLabel, String[] subCommandArgs) {
        if (subCommandArgs.length < 1) {
            sender.sendMessage(ColorList.ERR + "Please specify a player.");
            sender.sendMessage(subCommand.getHelpMessage(baseCommandLabel, subCommandLabel));
            return;
        }
        if (subCommandArgs.length > 1) {
            sender.sendMessage(ColorList.ERR + "To many arguments");
            sender.sendMessage(subCommand.getHelpMessage(baseCommandLabel, subCommandLabel));
            return;
        }
        PData pData = playerDataHandler.getPData(subCommandArgs[0]);
        if (pData == null) {
            sender.sendMessage(ColorList.ERR + "Player " + ColorList.ERR_ARGS + subCommandArgs[0] + ColorList.ERR + " not found");
            return;
        }
        if (!pData.isGroup("banned")) {
            sender.sendMessage(ColorList.ERR_ARGS + pData.userName() + ColorList.ERR + " is not currently banned");
            return;
        }
        Data data = pData.getData("bandata");
        if (data == null) {
            sender.sendMessage(ColorList.ERR + "No bandata found for player " + ColorList.ERR_ARGS + pData.userName());
            return;
        }
        BData banData = DataParser.parseFromlist(data);
        if (banData == null) {
            sender.sendMessage(ColorList.ERR + "Error parsing BanData");
            return;
        }
        Ban[] bans = banData.getBans();
        String[] permissionGroupsToSet = null;
        for (int i = bans.length - 1; i >= 0; i--) {
            Ban b = bans[i];
            String[] oldGroups = b.getOldGroups();
            if (oldGroups.length == 0) {
                continue;
            }
            List<String> groups = new ArrayList<String>();
            for (String str : oldGroups) {
                if (!(str.equalsIgnoreCase("banned")) || (groups.contains(str))) {
                    groups.add(str);
                }
            }
            if (!groups.isEmpty()) {
                permissionGroupsToSet = groups.toArray(new String[groups.size()]);
                break;
            }
        }
        if (permissionGroupsToSet == null) {
            sender.sendMessage(ColorList.ERR + "Error parsing BanData. No previous groups found");
            return;
        }
        if (!(PlayerData.isVaultLoaded())) {
            sender.sendMessage(ColorList.ERR + "Vault permission handler not found");
        }
        List<String> rawData;
        if (pData.hasData("rankrecord")) {
            rawData = new ArrayList<String>(Arrays.asList(pData.getData("rankrecord").getData()));
        } else {
            rawData = new ArrayList<String>();
        }
        PlayerData.getPermissionHandler().playerRemoveGroup((String) null, pData.userName(), "Banned");
        for (String group : permissionGroupsToSet) {
            PlayerData.getPermissionHandler().playerAddGroup((String) null, pData.userName(), group);
        }
        rawData.add("SET " + sender.getName() + " " + Arrays.toString(permissionGroupsToSet) + " " + System.currentTimeMillis());
        Data finalData = new Data("rankrecord", rawData.toArray(new String[rawData.size()]));
        pData.addData(finalData);
        Bukkit.getServer().broadcastMessage(String.format(ColorList.BROADCAST_NAME_FORMAT, "BanData") + ColorList.NAME + pData.userName() + ColorList.BROADCAST + " was unbanned by " + ColorList.NAME + sender.getName());
        sender.sendMessage(ColorList.NAME + pData.userName() + " has been set to: " + getString(permissionGroupsToSet));
    }

    private String getString(String[] strings) {
        if (strings.length == 0) {
            return "None";
        }
        StringBuilder resultBuilder = new StringBuilder(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            String string = strings[i];
            resultBuilder.append(", ").append(string);
        }
        return resultBuilder.toString();
    }
}
