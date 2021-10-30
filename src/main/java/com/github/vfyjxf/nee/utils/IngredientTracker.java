package com.github.vfyjxf.nee.utils;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.me.ItemRepo;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import com.github.vfyjxf.nee.config.NEEConfig;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;

import java.util.ArrayList;
import java.util.List;

public class IngredientTracker {

    private final List<Ingredient> ingredients = new ArrayList<>();

    public IngredientTracker(GuiContainer gui, IRecipeHandler recipe, int recipeIndex) {
        List<IAEItemStack> craftableStacks = getCraftableStacks(gui);
        List<PositionedStack> requiredIngredients = new ArrayList<>();
        for (PositionedStack positionedStack : recipe.getIngredientStacks(recipeIndex)) {
            boolean find = false;
            for (PositionedStack currentIngredient : requiredIngredients) {
                boolean areItemStackEquals = currentIngredient.items[0].isItemEqual(positionedStack.items[0]) && ItemStack.areItemStackTagsEqual(currentIngredient.items[0], positionedStack.items[0]);
                if (areItemStackEquals) {
                    currentIngredient.items[0].stackSize += positionedStack.items[0].stackSize;
                    find = true;
                }
            }

            if (!find) {
                requiredIngredients.add(positionedStack);
            }

        }
        for (PositionedStack requiredIngredient : requiredIngredients) {
            Ingredient ingredient = new Ingredient(requiredIngredient);
            ingredients.add(ingredient);

            for (IAEItemStack stack : NEEConfig.matchOtherItems ? this.getStorageStacks(gui) : craftableStacks) {
                if (requiredIngredient.contains(stack.getItemStack())) {
                    if (stack.isCraftable()) {
                        ingredient.setCraftableIngredient(stack.getItemStack());
                    }
                    ingredient.addCurrentCount(stack.getItemStack().stackSize);
                }
            }
        }
    }

    public IngredientTracker(GuiContainer gui, List<PositionedStack> requiredIngredients) {

        for (PositionedStack positionedStack : requiredIngredients) {
            this.ingredients.add(new Ingredient(positionedStack));
        }

        List<IAEItemStack> craftableStacks = this.getCraftableStacks(gui);

        //set craftable stack
        for (Ingredient ingredient : this.ingredients) {
            for (IAEItemStack stack : craftableStacks) {
                if (ingredient.getIngredients().contains(stack.getItemStack())) {
                    if (!ingredient.isCraftable()) {
                        ingredient.setCraftableIngredient(stack.getItemStack());
                    }
                    if (stack.getStackSize() > 0) {
                        int missingCount = (int) ingredient.getMissingCount();
                        ingredient.addCurrentCount(stack.getStackSize());
                        if (ingredient.requiresToCraft()) {
                            stack.setStackSize(0);
                        } else {
                            stack.setStackSize(stack.getStackSize() - missingCount);
                        }
                    }
                }
            }
        }

        if (NEEConfig.matchOtherItems) {
            List<IAEItemStack> otherStacks = this.getStorageStacks(gui);
            otherStacks.removeAll(craftableStacks);
            for (Ingredient ingredient : this.ingredients) {
                for (IAEItemStack stack : otherStacks) {
                    if (ingredient.requiresToCraft() && ingredient.getIngredients().contains(stack.getItemStack())) {
                        int missingCount = (int) ingredient.getMissingCount();
                        ingredient.addCurrentCount(stack.getStackSize());
                        if (ingredient.requiresToCraft()) {
                            stack.setStackSize(0);
                        } else {
                            stack.setStackSize(stack.getStackSize() - missingCount);
                        }
                    }
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private List<IAEItemStack> getCraftableStacks(GuiContainer gui) {
        List<IAEItemStack> craftableStacks = new ArrayList<>();
        IItemList<IAEItemStack> list = null;
        try {
            if (!GuiUtils.isGuiWirelessCrafting(gui)) {
                ItemRepo repo = (ItemRepo) ReflectionHelper.findField(GuiMEMonitorable.class, "repo").get(gui);
                list = (IItemList<IAEItemStack>) ReflectionHelper.findField(ItemRepo.class, "list").get(repo);
            } else {
                //wireless crafting terminal support
                net.p455w0rd.wirelesscraftingterminal.client.me.ItemRepo repo = (net.p455w0rd.wirelesscraftingterminal.client.me.ItemRepo) ReflectionHelper.findField(GuiWirelessCraftingTerminal.class, "repo").get(gui);
                list = (IItemList<IAEItemStack>) ReflectionHelper.findField(net.p455w0rd.wirelesscraftingterminal.client.me.ItemRepo.class,
                        "list").get(repo);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (list != null) {
            for (IAEItemStack stack : list) {
                if (stack.isCraftable()) {
                    craftableStacks.add(stack.copy());
                }
            }
        }
        return craftableStacks;
    }

    @SuppressWarnings("unchecked")
    private List<IAEItemStack> getStorageStacks(GuiContainer gui) {
        List<IAEItemStack> list = new ArrayList<>();
        try {
            if (!GuiUtils.isGuiWirelessCrafting(gui)) {
                ItemRepo repo = (ItemRepo) ReflectionHelper.findField(GuiMEMonitorable.class, "repo").get(gui);
                for (IAEItemStack stack : (IItemList<IAEItemStack>) ReflectionHelper.findField(ItemRepo.class, "list").get(repo)) {
                    list.add(stack.copy());
                }
            } else {
                //wireless crafting terminal support
                net.p455w0rd.wirelesscraftingterminal.client.me.ItemRepo repo = (net.p455w0rd.wirelesscraftingterminal.client.me.ItemRepo) ReflectionHelper.findField(GuiWirelessCraftingTerminal.class, "repo").get(gui);
                for (IAEItemStack stack : (IItemList<IAEItemStack>) ReflectionHelper.findField(net.p455w0rd.wirelesscraftingterminal.client.me.ItemRepo.class,
                        "list").get(repo)) {
                    list.add(stack.copy());
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<ItemStack> getRequireToCraftStacks() {
        List<ItemStack> requireToCraftStacks = new ArrayList<>();
        for (Ingredient ingredient : this.ingredients) {
            ItemStack craftableStack = ingredient.getCraftableIngredient();
            if (craftableStack != null && ingredient.requiresToCraft()) {
                ItemStack requireStack = craftableStack.copy();
                requireStack.stackSize = (int) ingredient.getMissingCount();
                requireToCraftStacks.add(requireStack);
            }
        }
        return requireToCraftStacks;
    }

    public void addAvailableStack(ItemStack stack) {
        for (Ingredient ingredient : this.ingredients) {
            if (ingredient.requiresToCraft()) {
                if (NEEConfig.matchOtherItems) {
                    if (stack.stackSize > 0 && ingredient.getIngredients().contains(stack)) {
                        int missingCount = (int) ingredient.getMissingCount();
                        ingredient.addCurrentCount(stack.stackSize);
                        if (ingredient.requiresToCraft()) {
                            stack.stackSize = 0;
                        } else {
                            stack.stackSize -= missingCount;
                        }
                        break;
                    }
                } else {
                    ItemStack craftableStack = ingredient.getCraftableIngredient();
                    if (craftableStack != null && craftableStack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(craftableStack, stack) && stack.stackSize > 0) {
                        int missingCount = (int) ingredient.getMissingCount();
                        ingredient.addCurrentCount(stack.stackSize);
                        if (ingredient.requiresToCraft()) {
                            stack.stackSize = 0;
                        } else {
                            stack.stackSize -= missingCount;
                        }
                        break;
                    }
                }
            }
        }
    }

}
