/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.bukkit.chat.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.skriptlang.skript.bukkit.chat.util.ComponentHandler;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Broadcast")
@Description("Broadcasts a message to the server.")
@Examples({
	"broadcast \"Welcome %player% to the server!\"",
	"broadcast \"Woah! It's a message!\""
})
@Since("1.0, 2.6 (broadcasting objects), 2.6.1 (using advanced formatting)")
public class EffBroadcast extends Effect {

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?>[] messages;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> messageExpr;
	@Nullable
	private Expression<World> worlds;

	static {
		Skript.registerEffect(EffBroadcast.class, "broadcast %objects% [(to|in) %-worlds%]");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		messageExpr = LiteralUtils.defendExpression(exprs[0]);
		if (messageExpr instanceof ExpressionList) {
			ExpressionList<?> exprList = (ExpressionList<?>) messageExpr;
			if (exprList.getAnd()) {
				messages = exprList.getExpressions();
			} else {
				messages = new Expression[]{CollectionUtils.getRandom(exprList.getExpressions())};
			}
		} else {
			messages = new Expression[]{messageExpr};
		}

		worlds = (Expression<World>) exprs[1];
		return LiteralUtils.canInitSafely(messageExpr);
	}

	@Override
	protected void execute(Event e) {
		if (worlds == null) {
			for (Component component : ComponentHandler.parseFromExpressions(e, messages))
				Bukkit.broadcast(component);
		} else {
			List<CommandSender> recipients = new ArrayList<>();
			for (World world : worlds.getArray(e))
				recipients.addAll(world.getPlayers());
			Audience audience = Audience.audience(recipients);
			for (Component component : ComponentHandler.parseFromExpressions(e, messages))
				audience.sendMessage(component);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "broadcast " + messageExpr.toString(e, debug) + (worlds == null ? "" : " to " + worlds.toString(e, debug));
	}

}
