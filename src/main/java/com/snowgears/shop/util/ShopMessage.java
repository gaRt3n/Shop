package com.snowgears.shop.util;

import com.snowgears.shop.Shop;
import com.snowgears.shop.ShopObject;
import com.snowgears.shop.ShopType;
import com.snowgears.shop.display.DisplayType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;


public class ShopMessage {

    private static HashMap<String, String> messageMap = new HashMap<String, String>();
    private static HashMap<String, String[]> shopSignTextMap = new HashMap<String, String[]>();
    private static String freePriceWord;
    private static HashMap<String, String> creationWords = new HashMap<String, String>();
    private static YamlConfiguration chatConfig;
    private static YamlConfiguration signConfig;

    public ShopMessage(Shop plugin) {

        File chatConfigFile = new File(plugin.getDataFolder(), "chatConfig.yml");
        chatConfig = YamlConfiguration.loadConfiguration(chatConfigFile);
        File signConfigFile = new File(plugin.getDataFolder(), "signConfig.yml");
        signConfig = YamlConfiguration.loadConfiguration(signConfigFile);

        loadMessagesFromConfig();
        loadSignTextFromConfig();
        loadCreationWords();

        freePriceWord = signConfig.getString("sign_text.zeroPrice");
    }

    public static String getCreationWord(String type){
        return creationWords.get(type);
    }

    public static String getFreePriceWord(){
        return freePriceWord;
    }

    public static String getMessage(String key, String subKey, ShopObject shop, Player player) {
        String message;
        if (subKey != null)
            message = messageMap.get(key + "_" + subKey);
        else
            message = messageMap.get(key);

        message = formatMessage(message, shop, player, false);
        return message;
    }

    public static String getUnformattedMessage(String key, String subKey) {
        String message;
        if (subKey != null)
            message = messageMap.get(key + "_" + subKey);
        else
            message = messageMap.get(key);
        return message;
    }

    //      # [item] : The name of the item in the transaction #
    //      # [item amount] : The amount of the item #
    //      # [barter item] : The name of the barter item in the transaction #
    //      # [barter item amount] : The amount of the barter item #
    //      # [user] : The name of the player who used the shop #
    //      # [owner] : The name of the shop owner #
    //      # [server name] : The name of the server #
    public static String formatMessage(String unformattedMessage, ShopObject shop, Player player, boolean forSign){
        if(unformattedMessage == null) {
            loadMessagesFromConfig();
            return "";
        }
        if(shop != null && shop.getItemStack() != null) {
            unformattedMessage = unformattedMessage.replace("[item amount]", "" + shop.getItemStack().getAmount());
            //dont replace [item] tag on first run through if its for a sign
            if(!forSign)
                unformattedMessage = unformattedMessage.replace("[item]", "" + Shop.getPlugin().getItemNameUtil().getName(shop.getItemStack()));
            unformattedMessage = unformattedMessage.replace("[item durability]", "" + shop.getItemDurabilityPercent(false));
        }
        if(shop != null && shop.getBarterItemStack() != null) {
            unformattedMessage = unformattedMessage.replace("[barter item amount]", "" + shop.getBarterItemStack().getAmount());
            //dont replace [barter item] tag on first run through if its for a sign
            if(!forSign)
                unformattedMessage = unformattedMessage.replace("[barter item]", "" + Shop.getPlugin().getItemNameUtil().getName(shop.getBarterItemStack()));
            unformattedMessage = unformattedMessage.replace("[barter item durability]", "" + shop.getItemDurabilityPercent(true));
        }
        if(shop != null) {
            if(shop.isAdminShop())
                unformattedMessage = unformattedMessage.replace("[owner]", "" + Bukkit.getServer().getServerName());
            else
                unformattedMessage = unformattedMessage.replace("[owner]", "" + shop.getOwnerName());
            unformattedMessage = unformattedMessage.replace("[price]", "" + shop.getPriceString());
            if(shop.getType() == ShopType.BARTER){
                String amountPerString = new DecimalFormat("#.##").format(shop.getPrice() / shop.getAmount()).toString();
                amountPerString = amountPerString + " " + Shop.getPlugin().getItemNameUtil().getName(shop.getBarterItemStack());
                unformattedMessage = unformattedMessage.replace("[price per item]", "" + amountPerString);
            }
            else
                unformattedMessage = unformattedMessage.replace("[price per item]", "" + shop.getPricePerItemString());
            unformattedMessage = unformattedMessage.replace("[shop type]", "" + ShopMessage.getCreationWord(shop.getType().toString().toUpperCase())); //sub in user's word for SELL,BUY,BARTER
            if(unformattedMessage.contains("[stock]"))
                unformattedMessage = unformattedMessage.replace("[stock]", "" + shop.getStock());
        }
        if(player != null) {
            unformattedMessage = unformattedMessage.replace("[user]", "" + player.getName());
            unformattedMessage = unformattedMessage.replace("[build limit]", "" + Shop.getPlugin().getShopListener().getBuildLimit(player));
        }
        unformattedMessage = unformattedMessage.replace("[server name]", "" + Bukkit.getServer().getServerName());

        if(forSign){
            if(unformattedMessage.contains("[item]") && shop != null && shop.getItemStack() != null){
                int tagLength = "[item]".length();
                String itemName = Shop.getPlugin().getItemNameUtil().getName(shop.getItemStack());
                int itemNameLength = itemName.length();
                int totalLength = unformattedMessage.length() - tagLength + itemNameLength;
                if(totalLength > 17){
                    String cutItemName = itemName.substring(0, (itemName.length()-(Math.abs(17 - totalLength))));
                    unformattedMessage = unformattedMessage.replace("[item]", cutItemName);
                }
                else{
                    unformattedMessage = unformattedMessage.replace("[item]", "" + Shop.getPlugin().getItemNameUtil().getName(shop.getItemStack()));
                }
            }
            if(unformattedMessage.contains("[barter item]") && shop != null && shop.getBarterItemStack() != null){
                int tagLength = "[barter item]".length();
                String itemName = Shop.getPlugin().getItemNameUtil().getName(shop.getBarterItemStack());
                int itemNameLength = itemName.length();
                int totalLength = unformattedMessage.length() - tagLength + itemNameLength;
                if(totalLength > 17){
                    String cutItemName = itemName.substring(0, (itemName.length()-(Math.abs(17 - totalLength))));
                    unformattedMessage = unformattedMessage.replace("[barter item]", cutItemName);
                }
                else{
                    unformattedMessage = unformattedMessage.replace("[barter item]", "" + Shop.getPlugin().getItemNameUtil().getName(shop.getBarterItemStack()));
                }
            }
        }

        unformattedMessage = ChatColor.translateAlternateColorCodes('&', unformattedMessage);
        return unformattedMessage;
    }

    //      # [amount] : The amount of items the shop is selling/buying/bartering #
    //      # [price] : The price of the items the shop is selling (adjusted to match virtual or physical currency) #
    //      # [owner] : The name of the shop owner #
    //      # [server name] : The name of the server #
    public static String[] getSignLines(ShopObject shop){
        String[] lines;
        if(shop.isAdminShop())
            lines = getUnformattedShopSignLines(shop.getType(), "admin");
        else if(Shop.getPlugin().getDisplayType() == DisplayType.NONE)
            lines = getUnformattedShopSignLines(shop.getType(), "no_display");
        else
            lines = getUnformattedShopSignLines(shop.getType(), "normal");

        for(int i=0; i<lines.length; i++) {
            lines[i] = lines[i].replace("[amount]", "" + shop.getAmount());
            if (shop.getType() == ShopType.BARTER)
                lines[i] = lines[i].replace("[price]", "" + (int) shop.getPrice());
            else
                lines[i] = lines[i].replace("[price]", shop.getPriceString());
            lines[i] = lines[i].replace("[owner]", "" + shop.getOwnerName());
            lines[i] = formatMessage(lines[i], shop, null, true);

            lines[i] = ChatColor.translateAlternateColorCodes('&', lines[i]);

            //TODO have smart way of cutting lines if too long so at least some of word can go on
//            if(lines[i].length() > 15)
//                lines[i]
        }
        return lines;
    }

    private static String[] getUnformattedShopSignLines(ShopType type, String subtype) {
        if (shopSignTextMap.get(type.toString()+"_"+subtype) == null) {
            String[] lines = new String[4];
            lines[0] = ChatColor.BOLD+"[shop]";
            if(type == ShopType.SELL || type == ShopType.BUY) {
                lines[1] = UtilMethods.capitalize(type.toString().toLowerCase()) + "ing: "+ChatColor.BOLD+"[amount]";
                lines[2] = ChatColor.GREEN+"[price]";
            }
            else {
                lines[1] = "Bartering:";
                lines[2] = ChatColor.GREEN+"[price]   for   [amount]";
            }
            if(subtype.equalsIgnoreCase("admin"))
                lines[3] = ChatColor.LIGHT_PURPLE+"[server name]";
            else
                lines[3] = "[owner]";
            return lines;
        }
        return shopSignTextMap.get(type.toString()+"_"+subtype).clone();
    }

    private static void loadMessagesFromConfig() {

        for (ShopType type : ShopType.values()) {
            messageMap.put(type.toString() + "_user", chatConfig.getString("transaction." + type.toString().toUpperCase() + ".user"));
            messageMap.put(type.toString() + "_owner", chatConfig.getString("transaction." + type.toString().toUpperCase() + ".owner"));

            messageMap.put(type.toString() + "_initialize", chatConfig.getString("interaction." + type.toString().toUpperCase() + ".initialize"));
            if(type == ShopType.BUY)
                messageMap.put(type.toString() + "_initializeAlt", chatConfig.getString("interaction." + type.toString().toUpperCase() + ".initializeAlt"));
            else if(type == ShopType.BARTER) {
                messageMap.put(type.toString() + "_initializeInfo", chatConfig.getString("interaction." + type.toString().toUpperCase() + ".initializeInfo"));
                messageMap.put(type.toString() + "_initializeBarter", chatConfig.getString("interaction." + type.toString().toUpperCase() + ".initializeBarter"));
            }
            messageMap.put(type.toString() + "_create", chatConfig.getString("interaction." + type.toString().toUpperCase() + ".create"));
            messageMap.put(type.toString() + "_destroy", chatConfig.getString("interaction." + type.toString().toUpperCase() + ".destroy"));
            messageMap.put(type.toString() + "_opDestroy", chatConfig.getString("interaction." + type.toString().toUpperCase() + ".opDestroy"));
            messageMap.put(type.toString() + "_opOpen", chatConfig.getString("interaction." + type.toString().toUpperCase() + ".opOpen"));

            messageMap.put(type.toString() + "_shopNoStock", chatConfig.getString("transaction_issue." + type.toString().toUpperCase() + ".shopNoStock"));
            messageMap.put(type.toString() + "_shopNoSpace", chatConfig.getString("transaction_issue." + type.toString().toUpperCase() + ".shopNoSpace"));
            messageMap.put(type.toString() + "_playerNoStock", chatConfig.getString("transaction_issue." + type.toString().toUpperCase() + ".playerNoStock"));
            messageMap.put(type.toString() + "_playerNoSpace", chatConfig.getString("transaction_issue." + type.toString().toUpperCase() + ".playerNoSpace"));

            messageMap.put(type.toString() + "_descriptionItem", chatConfig.getString("description."+type.toString().toUpperCase()+".item"));
            if(type == ShopType.BARTER)
                messageMap.put(type.toString() + "_descriptionBarterItem", chatConfig.getString("description." + type.toString().toUpperCase() + ".barterItem"));
            messageMap.put(type.toString() + "_descriptionPrice", chatConfig.getString("description."+type.toString().toUpperCase()+".price"));
            messageMap.put(type.toString() + "_descriptionPricePerItem", chatConfig.getString("description."+type.toString().toUpperCase()+".pricePerItem"));
        }
        messageMap.put("description_stock", chatConfig.getString("description.stock"));
        messageMap.put("description_stockAdmin", chatConfig.getString("description.stockAdmin"));

        messageMap.put("permission_use", chatConfig.getString("permission.use"));
        messageMap.put("permission_create", chatConfig.getString("permission.create"));
        messageMap.put("permission_destroy", chatConfig.getString("permission.destroy"));
        messageMap.put("permission_buildLimit", chatConfig.getString("permission.buildLimit"));

        messageMap.put("interactionIssue_line2", chatConfig.getString("interaction_issue.createLine2"));
        messageMap.put("interactionIssue_line3", chatConfig.getString("interaction_issue.createLine3"));
        messageMap.put("interactionIssue_noItem", chatConfig.getString("interaction_issue.createNoItem"));
        messageMap.put("interactionIssue_direction", chatConfig.getString("interaction_issue.createDirection"));
        messageMap.put("interactionIssue_sameItem", chatConfig.getString("interaction_issue.createSameItem"));
        messageMap.put("interactionIssue_displayRoom", chatConfig.getString("interaction_issue.createDisplayRoom"));
        messageMap.put("interactionIssue_createInsufficientFunds", chatConfig.getString("interaction_issue.createInsufficientFunds"));
        messageMap.put("interactionIssue_destroyInsufficientFunds", chatConfig.getString("interaction_issue.destroyInsufficientFunds"));
        messageMap.put("interactionIssue_initialize", chatConfig.getString("interaction_issue.initializeOtherShop"));
        messageMap.put("interactionIssue_destroyChest", chatConfig.getString("interaction_issue.destroyChest"));
        messageMap.put("interactionIssue_useOwnShop", chatConfig.getString("interaction_issue.useOwnShop"));
        messageMap.put("interactionIssue_adminOpen", chatConfig.getString("interaction_issue.adminOpen"));
        messageMap.put("interactionIssue_worldBlacklist", chatConfig.getString("interaction_issue.worldBlacklist"));

    }

    private void loadSignTextFromConfig() {
        Set<String> allTypes = signConfig.getConfigurationSection("sign_text").getKeys(false);
        for (String typeString : allTypes) {

            ShopType type = null;
            try { type = ShopType.valueOf(typeString);}
            catch (IllegalArgumentException e){}

            if (type != null) {
                String[] normalLines = new String[4];
                Set<String> normalLineNumbers = signConfig.getConfigurationSection("sign_text." + typeString + ".normal").getKeys(false);

                int i = 0;
                for (String number : normalLineNumbers) {
                    String message = signConfig.getString("sign_text." + typeString + ".normal." + number);
                    if (message == null)
                        normalLines[i] = "";
                    else
                        normalLines[i] = message;
                    i++;
                }

                this.shopSignTextMap.put(type.toString() + "_normal", normalLines);

                String[] adminLines = new String[4];
                Set<String> adminLineNumbers = signConfig.getConfigurationSection("sign_text." + typeString + ".admin").getKeys(false);

                i = 0;
                for (String number : adminLineNumbers) {
                    String message = signConfig.getString("sign_text." + typeString + ".admin." + number);
                    if (message == null)
                        adminLines[i] = "";
                    else
                        adminLines[i] = message;
                    i++;
                }

                this.shopSignTextMap.put(type.toString() + "_admin", adminLines);

                if(Shop.getPlugin().getDisplayType() == DisplayType.NONE) {
                    String[] noDisplayLines = new String[4];
                    Set<String> noDisplayLineNumbers = signConfig.getConfigurationSection("sign_text." + typeString + ".no_display").getKeys(false);

                    i = 0;
                    for (String number : noDisplayLineNumbers) {
                        String message = signConfig.getString("sign_text." + typeString + ".no_display." + number);
                        if (message == null)
                            noDisplayLines[i] = "";
                        else
                            noDisplayLines[i] = message;
                        i++;
                    }

                    this.shopSignTextMap.put(type.toString() + "_no_display", noDisplayLines);
                }
            }
        }
    }

    private void loadCreationWords(){
        String shopString = signConfig.getString("sign_creation.SHOP");
        if(shopString != null)
            creationWords.put("SHOP", shopString.toLowerCase());
        else
            creationWords.put("SHOP", "[shop]");

        String sellString = signConfig.getString("sign_creation.SELL");
        if(sellString != null)
            creationWords.put("SELL", sellString.toLowerCase());
        else
            creationWords.put("SELL", "sell");

        String buyString = signConfig.getString("sign_creation.BUY");
        if(buyString != null)
            creationWords.put("BUY", buyString.toLowerCase());
        else
            creationWords.put("BUY", "buy");

        String barterString = signConfig.getString("sign_creation.BARTER");
        if(barterString != null)
            creationWords.put("BARTER", barterString.toLowerCase());
        else
            creationWords.put("BARTER", "barter");

        String adminString = signConfig.getString("sign_creation.ADMIN");
        if(adminString != null)
            creationWords.put("ADMIN", adminString.toLowerCase());
        else
            creationWords.put("ADMIN", "admin");
    }
}