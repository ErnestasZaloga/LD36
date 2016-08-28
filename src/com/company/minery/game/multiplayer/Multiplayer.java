package com.company.minery.game.multiplayer;

import com.company.minery.game.multiplayer.messages.ClientAssignmentMessage;
import com.company.minery.game.multiplayer.messages.ImpulseMessage;
import com.company.minery.game.multiplayer.messages.PlayerMessage;
import com.company.minery.game.multiplayer.messages.SpearMessage;
import com.company.minery.game.multiplayer.messages.WorldStateMessage;
import com.company.minery.utils.kryonet.EndPoint;
import com.esotericsoftware.kryo.Kryo;

public final class Multiplayer {

	public static void register(final EndPoint endPoint) {
		final Kryo kryo = endPoint.getKryo();
		
		kryo.register(PlayerMessage.class);
		kryo.register(SpearMessage.class);
		kryo.register(WorldStateMessage.class);
		kryo.register(ClientAssignmentMessage.class);
		kryo.register(ImpulseMessage.class);
		kryo.register(PlayerMessage[].class);
		kryo.register(SpearMessage[].class);
	}
	
}