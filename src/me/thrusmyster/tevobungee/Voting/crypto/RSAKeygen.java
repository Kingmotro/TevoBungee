package me.thrusmyster.tevobungee.Voting.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;

import me.thrusmyster.tevobungee.TevoBungee;

public class RSAKeygen {
	
	public static KeyPair generate(int bits)
	throws Exception
	{
		TevoBungee.getInstance().getUtilLogger().info("Generating RSA key pair...");
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(bits, RSAKeyGenParameterSpec.F4);
		
		keygen.initialize(spec);
		return keygen.generateKeyPair();
	}
	
}
