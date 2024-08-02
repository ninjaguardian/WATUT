package com.corosus.watut.command;

import com.corosus.coroutil.command.CommandCoroConfig;
import com.corosus.watut.WatutMod;
import com.corosus.watut.config.CustomArmCorrections;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public class CommandWatutReloadJSON {
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands.literal(getCommandName())
			.then(literal("reloadJSON")
				.executes(context -> {
					if (CustomArmCorrections.loadJsonConfigs()) {
						context.getSource().sendSuccess(() -> Component.literal("Reloaded " + WatutMod.configJSONName), true);
					} else {
						context.getSource().sendSuccess(() -> Component.literal("Failed to load " + WatutMod.configJSONName + ", check for format errors"), true);
					}
					return Command.SINGLE_SUCCESS;
				})
			)
		);
	}

	public static String getCommandName() {
		return WatutMod.MODID;
	}
}
