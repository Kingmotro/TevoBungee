package me.thrusmyster.tevobungee.Voting.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.xml.bind.DatatypeConverter;

public class RSAIO {

	public static void save(File dir, KeyPair keys)
	throws Exception
	{
		PrivateKey privatekey = keys.getPrivate();
		PublicKey publickey = keys.getPublic();
		
		X509EncodedKeySpec publicspec = new X509EncodedKeySpec(publickey.getEncoded());
		
		FileOutputStream out = new FileOutputStream(dir + "/public.key");
		out.write(DatatypeConverter.printBase64Binary(publicspec.getEncoded()).getBytes());
		
		out.close();
		
		PKCS8EncodedKeySpec privatespec = new PKCS8EncodedKeySpec(privatekey.getEncoded());
		
		out = new FileOutputStream(dir + "/private.key");
		out.write(DatatypeConverter.printBase64Binary(privatespec.getEncoded()).getBytes());
		
		out.close();
	}

	public static KeyPair load(File dir)
	throws Exception
	{
		File publickeyFile = new File(dir + "/public.key");
		FileInputStream in = new FileInputStream(dir + "/public.key");
		byte[] encodedpublicKey = new byte[(int)publickeyFile.length()];
		in.read(encodedpublicKey);
		encodedpublicKey = DatatypeConverter.parseBase64Binary(new String(encodedpublicKey));
		
		in.close();
		
		File privatekeyFile = new File(dir + "/private.key");
		in = new FileInputStream(dir + "/private.key");
		byte[] encodedprivateKey = new byte[(int)privatekeyFile.length()];
		in.read(encodedprivateKey);
		encodedprivateKey = DatatypeConverter.parseBase64Binary(new String(encodedprivateKey));
		
		in.close();
		
		KeyFactory keyfactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec publickeySpec = new X509EncodedKeySpec(encodedpublicKey);
		
		PublicKey publickey = keyfactory.generatePublic(publickeySpec);
		
		PKCS8EncodedKeySpec privatekeySpec = new PKCS8EncodedKeySpec(encodedprivateKey);
		
		PrivateKey privatekey = keyfactory.generatePrivate(privatekeySpec);
		return new KeyPair(publickey, privatekey);
		
	}
	
}
