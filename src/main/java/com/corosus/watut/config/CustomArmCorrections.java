package com.corosus.watut.config;

import com.google.gson.Gson;
import com.ibm.icu.impl.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CustomArmCorrections {

    private static HeldItemArmAdjustmentLists heldItemArmAdjustmentLists = null;

    public static void loadJsonConfigs() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("./config/watut-item-arm-adjustments.json")) {
            heldItemArmAdjustmentLists = gson.fromJson(reader, HeldItemArmAdjustmentLists.class);

            // Use the parsed object
            //System.out.println(heldItemArmAdjustmentLists);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HeldItemArmAdjustmentLists getHeldItemArmAdjustmentLists() {
        return heldItemArmAdjustmentLists;
    }

    public static Vector3f getAdjustmentForArm(ItemStack stackRightArm, ItemStack stackLeftArm, EquipmentSlot equipmentSlot) {
        try {
            for (HeldItemArmAdjustment heldItemArmAdjustment : getHeldItemArmAdjustmentLists().getHeldItemArmAdjustments()) {
                boolean matchRight = filterMatches(heldItemArmAdjustment, stackRightArm);
                boolean matchLeft = filterMatches(heldItemArmAdjustment, stackLeftArm);
                boolean matchFound = (matchRight && (equipmentSlot == EquipmentSlot.MAINHAND || heldItemArmAdjustment.isEitherHandMatchAppliesBothHandAdjustments()))
                || (matchLeft && (equipmentSlot == EquipmentSlot.OFFHAND || heldItemArmAdjustment.isEitherHandMatchAppliesBothHandAdjustments()));

                if (matchFound) {
                    float adjX = 0;
                    float adjY = 0;
                    float adjZ = 0;
                    if (equipmentSlot == EquipmentSlot.MAINHAND) {
                        if (heldItemArmAdjustment.getAdjustment().getMainhandX().toLowerCase().startsWith("disable")) {
                            adjX = Float.MAX_VALUE;
                        } else {
                            adjX = Float.parseFloat(heldItemArmAdjustment.getAdjustment().getMainhandX());
                        }
                        if (heldItemArmAdjustment.getAdjustment().getMainhandY().toLowerCase().startsWith("disable")) {
                            adjY = Float.MAX_VALUE;
                        } else {
                            adjY = Float.parseFloat(heldItemArmAdjustment.getAdjustment().getMainhandY());
                        }
                        if (heldItemArmAdjustment.getAdjustment().getMainhandZ().toLowerCase().startsWith("disable")) {
                            adjZ = Float.MAX_VALUE;
                        } else {
                            adjZ = Float.parseFloat(heldItemArmAdjustment.getAdjustment().getMainhandZ());
                        }
                    } else if (equipmentSlot == EquipmentSlot.OFFHAND) {
                        if (heldItemArmAdjustment.getAdjustment().getOffhandX().toLowerCase().startsWith("disable")) {
                            adjX = Float.MAX_VALUE;
                        } else {
                            adjX = Float.parseFloat(heldItemArmAdjustment.getAdjustment().getOffhandX());
                        }
                        if (heldItemArmAdjustment.getAdjustment().getOffhandY().toLowerCase().startsWith("disable")) {
                            adjY = Float.MAX_VALUE;
                        } else {
                            adjY = Float.parseFloat(heldItemArmAdjustment.getAdjustment().getOffhandY());
                        }
                        if (heldItemArmAdjustment.getAdjustment().getOffhandZ().toLowerCase().startsWith("disable")) {
                            adjZ = Float.MAX_VALUE;
                        } else {
                            adjZ = Float.parseFloat(heldItemArmAdjustment.getAdjustment().getOffhandZ());
                        }
                    }

                    return new Vector3f(adjX, adjY, adjZ);

                    //return Pair.of(vecMain, vecOff);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Vector3f(0, 0, 0);
        }
        return new Vector3f(0, 0, 0);
    }

    private static boolean filterMatches(HeldItemArmAdjustment heldItemArmAdjustment, ItemStack stack) {
        //using OR logic
        boolean matchFound = false;
        for (String filter : heldItemArmAdjustment.getFilters()) {
            String fullname = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();//stack.getItem().get.toString();

            String modID = fullname.split(":")[0];
            String name = fullname.split(":")[1];

            //mod id match check
            if (filter.contains("@")) {
                if (filter.substring(1).equals(modID)) {
                    matchFound = true;
                    break;
                }
            }

            //perfect match
            if (filter.equals(fullname)) {
                matchFound = true;
                break;
            }

            if (filter.startsWith("*") && filter.endsWith("*")) {
                String search = filter.replace("*", "").replace("*", "");
                if (fullname.contains(search)) {
                    matchFound = true;
                    break;
                }
            } else if (filter.startsWith("*")) {
                String search = fullname.replace("*", "");
                if (fullname.endsWith(search)) {
                    matchFound = true;
                    break;
                }
            } else if (filter.endsWith("*")) {
                String search = fullname.replace("*", "");
                if (fullname.startsWith(search)) {
                    matchFound = true;
                    break;
                }
            }
        }
        return matchFound;
    }
}

class HeldItemArmAdjustmentLists {
    private List<HeldItemArmAdjustment> held_item_arm_adjustments;

    // Getter and setter
    public List<HeldItemArmAdjustment> getHeldItemArmAdjustments() {
        return held_item_arm_adjustments;
    }

    public void setHeldItemArmAdjustments(List<HeldItemArmAdjustment> held_item_arm_adjustments) {
        this.held_item_arm_adjustments = held_item_arm_adjustments;
    }

    @Override
    public String toString() {
        return "RootObject{" +
                "held_item_arm_adjustments=" + held_item_arm_adjustments +
                '}';
    }
}

class HeldItemArmAdjustment {
    private List<String> filters;
    private Adjustment adjustment;
    private boolean eitherHandMatchAppliesBothHandAdjustments = false;

    // Getters and setters
    public boolean isEitherHandMatchAppliesBothHandAdjustments() {
        return eitherHandMatchAppliesBothHandAdjustments;
    }

    public void setEitherHandMatchAppliesBothHandAdjustments(boolean eitherHandMatchAppliesBothHandAdjustments) {
        this.eitherHandMatchAppliesBothHandAdjustments = eitherHandMatchAppliesBothHandAdjustments;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public Adjustment getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(Adjustment adjustment) {
        this.adjustment = adjustment;
    }

    @Override
    public String toString() {
        return "HeldItemArmAdjustment{" +
                ", filters=" + filters +
                ", adjustment=" + adjustment +
                '}';
    }
}

class Adjustment {
    private String mainhandX = "0";
    private String offhandX = "0";
    private String mainhandY = "0";
    private String offhandY = "0";
    private String mainhandZ = "0";
    private String offhandZ = "0";

    // Getters and setters
    public String getMainhandX() {
        return mainhandX;
    }

    public void setMainhandX(String mainhandX) {
        this.mainhandX = mainhandX;
    }

    public String getOffhandX() {
        return offhandX;
    }

    public void setOffhandX(String offhandX) {
        this.offhandX = offhandX;
    }

    public String getMainhandY() {
        return mainhandY;
    }

    public void setMainhandY(String mainhandY) {
        this.mainhandY = mainhandY;
    }

    public String getOffhandY() {
        return offhandY;
    }

    public void setOffhandY(String offhandY) {
        this.offhandY = offhandY;
    }

    public String getMainhandZ() {
        return mainhandZ;
    }

    public void setMainhandZ(String mainhandZ) {
        this.mainhandZ = mainhandZ;
    }

    public String getOffhandZ() {
        return offhandZ;
    }

    public void setOffhandZ(String offhandZ) {
        this.offhandZ = offhandZ;
    }

    @Override
    public String toString() {
        return "Adjustment{" +
                "mainhandX='" + mainhandX + '\'' +
                ", offhandX='" + offhandX + '\'' +
                '}';
    }
}