/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.sodapop.impl;

/*import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Vector;
*/
//import javax.crypto.Cipher;
//import javax.crypto.KeyGenerator;
//import javax.crypto.SecretKey;
//import javax.crypto.SecretKeyFactory;
//import javax.crypto.spec.DESKeySpec;

//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.util.encoders.Base64;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class CryptUtil {
	/*private static final String cipherTransformation = "DES/ECB/PKCS5Padding";
	private static final String keyFileName = "sodapop.key";
	private static final String randomizationAlgorithm = "SHA1PRNG";
	private static final String secretKeyAlgorithm = "DES";
	
	private static boolean tryMore = true;
//	private static SecretKey skey = null;
	
	public static void main(String[] args) {
		try {
			Security.addProvider(new BouncyCastleProvider());
			final File keyFile = new File(keyFileName);
			final Vector v = new Vector();
			// SecretKey mainkey = generateKey(keyFile);
			SecretKey mainkey = readKey(keyFile);
			
			new Thread() {
				public void run() {
					SecretKey myKey = null;
					try {
						myKey = readKey(keyFile); 
						while (true) {
							synchronized (v) {
								if (v.isEmpty()) {
									if (!tryMore)
										break;
									try { v.wait(); } catch (Exception x1) {}
								} else {
									System.out.println("Decrypted: "+decrypt(v.remove(0).toString(), myKey));
								}
							}
						}
						
						tryMore = true;
						String original = "This is the simple test #";
						for (int i=6; i<11; i++) {
							synchronized (v) {
								v.add(encrypt(original+i, myKey));
								v.notify();
							}
							System.out.println("Encrypted: "+original+i);
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
					tryMore = false;
					while (true)
						synchronized (v) {
							if (v.isEmpty())
								break;
							else
								v.notify();
						}
				}
			}.start();

			String original = "This is the simple test #";
			for (int i=1; i<6; i++) {
				synchronized (v) {
					v.add(encrypt(original+i, mainkey));
					v.notify();
				}
				System.out.println("Encrypted: "+original+i);
			}
			
			tryMore = false;
			while (!tryMore)
				synchronized (v) {
					if (v.isEmpty())
						try { v.wait(); } catch (Exception x1) {}
					else
						v.notify();
				}
			
			while (true) {
				synchronized (v) {
					if (v.isEmpty()) {
						if (!tryMore)
							break;
						try { v.wait(); } catch (Exception x1) {}
					} else {
						System.out.println("Decrypted: "+decrypt(v.remove(0).toString(), mainkey));
					}
				}
			}
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	static String init(String dir) throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		File keyFile = new File(dir + keyFileName);
		
		boolean newKey = false;
		try {
			skey = readKey(keyFile);
		} catch (Exception e) {
			skey = generateKey(keyFile);
			newKey = true;
		}
		
		if (skey == null)
			throw new SecurityException("Missing the secret key for message exchange!");
		
		if (newKey)
			return "New Key generated. Please copy "
			+  keyFile.getAbsolutePath()
			+ " to the confadmin folder of all the other instances of sodapop in your ensemble!";
		else
			return "Cryptography utils initialized successfully!";
	}
	
	static String decrypt(String cipher) throws Exception {
		return decrypt(cipher, skey);
	}
	
	private static String decrypt(String cipher, SecretKey skey) throws Exception {
		Cipher desCipher = Cipher.getInstance(cipherTransformation);
		desCipher.init(Cipher.DECRYPT_MODE, skey);
		return new String(desCipher.doFinal(Base64.decode(cipher)));
	}
	
	static String encrypt(String clear) throws Exception {
		return encrypt(clear, skey);
	}
	
	private static String encrypt(String clear, SecretKey skey) throws Exception {
		Cipher desCipher = Cipher.getInstance(cipherTransformation);
		desCipher.init(Cipher.ENCRYPT_MODE, skey);
		return new String(Base64.encode(desCipher.doFinal(clear.getBytes())));
	}
	
	private static SecretKey generateKey(File keyFile) throws Exception {
		KeyGenerator keyGen = KeyGenerator.getInstance(secretKeyAlgorithm);
		keyGen.init(SecureRandom.getInstance(randomizationAlgorithm));
		SecretKey skey = keyGen.generateKey();
		SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(secretKeyAlgorithm);
		DESKeySpec keyspec = (DESKeySpec) keyfactory.getKeySpec(skey,
				DESKeySpec.class);
		byte[] rawkey = keyspec.getKey();
		FileOutputStream out = new FileOutputStream(keyFile);
		out.write(rawkey);
		out.close();
		return skey;
	}
	
	public void generateKeyPair() throws Exception {
		KeyPairGenerator keyGen =
		    KeyPairGenerator.getInstance("DSA", "SUN");
		SecureRandom random =
		    SecureRandom.getInstance(randomizationAlgorithm, "SUN");
		keyGen.initialize(1024, random);
		KeyPair pair = keyGen.generateKeyPair();
		PrivateKey priv = pair.getPrivate();
		PublicKey pub = pair.getPublic();
		byte[] key = pub.getEncoded();
	    FileOutputStream keyfos = new FileOutputStream("public.key");
	    keyfos.write(key);
	    keyfos.close();
	    key = priv.getEncoded();
	    keyfos = new FileOutputStream("private.key");
	    keyfos.write(key);
	    keyfos.close();
	}

	/*private static SecretKey readKey(File keyFile) throws Exception {
		DataInputStream in = new DataInputStream(new FileInputStream(keyFile));
	    byte[] rawkey = new byte[(int) keyFile.length()];
	    in.readFully(rawkey);
	    in.close();
		DESKeySpec keyspec = new DESKeySpec(rawkey);
		SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(secretKeyAlgorithm);
		return keyfactory.generateSecret(keyspec);
	}*/
}
