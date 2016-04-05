package com.snowgears.shop.utils;

import com.snowgears.shop.Shop;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class ItemNameUtil {

    private Map<String, String> names = new HashMap<String, String>();

    public ItemNameUtil() {

        try {
            InputStream stream = getClass().getResourceAsStream("/com/snowgears/shop/utils/items.tsv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            String row;
                while ((row = reader.readLine()) != null) {
                    row = row.trim();
                    if (row.isEmpty())
                        continue;
                    String[] cols = row.split("\t");
                    String name = cols[2];
                    String id = cols[0];
                    String metadata = cols[1];
                    String idAndMetadata = metadata.equals("0") ? id : (id + ":" + metadata);
                    names.put(idAndMetadata, name);
                }
            } catch (IOException e) {
                System.out.println("[Shop] ERROR! Unable to initialize item name buffer reader. Using default spigot item names.");
                return;
            }
    }

    @SuppressWarnings("deprecation")
    public String getName(ItemStack item){
        if(item == null)
            return "";

        if(item.getItemMeta().getDisplayName() != null)
            return item.getItemMeta().getDisplayName();

        String format = ""+item.getTypeId()+":"+item.getData().getData();
        String name = names.get(format);
        if(name != null)
            return name;
        return getBackupName(item.getType());
    }

    @SuppressWarnings("deprecation")
    public String getName(Material material){
        String format = ""+material.getId()+":0";
        String name = names.get(format);
        if(name != null)
            return name;
        return getBackupName(material);
    }

    private String getBackupName(Material material){
        return UtilMethods.capitalize(material.name().replace("_", " ").toLowerCase());
    }
}