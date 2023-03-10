package com.maimiao.psd_see.common;
/**************************************************************************
	ADOBE SYSTEMS INCORPORATED
	 Copyright 2010 Adobe Systems Incorporated
	 All Rights Reserved.

	NOTICE:  Adobe permits you to use, modify, and distribute this file
	in accordance with the terms of the Adobe license agreement accompanying
	it.  If you have received this file from a source other than Adobe, then
	your use, modification, or distribution of it requires the prior written
	permission of Adobe.
**************************************************************************/

/**  Filename: EncryptDecrypt.java
	@author Thomas Ruark, Photoshop Engineering, Adobe Systems Incorporated
*/


/** this file is the same as in the AndroidExample except this line */
// package corp.adobe.photoshopevents;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecrypt {
    private Cipher mECipher;
    private Cipher mDCipher;

    // these must match the values used in Photoshop DO NOT CHANGE
    // get the password from the dialog settings in Photoshop
    private byte [] SALT = { 'A', 'd', 'o', 'b', 'e', ' ', 'P', 'h', 'o', 't', 'o', 's', 'h', 'o', 'p' };
    private final int ITERATION_COUNT = 1000;
    private final int KEY_LENGTH = 24;

    public EncryptDecrypt(String passPhrase) throws InvalidAlgorithmParameterException,
					     NoSuchPaddingException, 
					     NoSuchAlgorithmException, 
					     InvalidKeyException {

       	DESedeKeySpec keySpec = new DESedeKeySpec(PBKDF2.deriveKey( passPhrase.getBytes(), SALT, ITERATION_COUNT, KEY_LENGTH ));
       	Key key = new SecretKeySpec(keySpec.getKey(), "DESede");
        mECipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        mDCipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        mECipher.init(Cipher.ENCRYPT_MODE, key, iv);
        mDCipher.init(Cipher.DECRYPT_MODE, key, iv);
	
    }

    public byte [] encrypt(byte [] inBytes) throws BadPaddingException, 
						   IllegalBlockSizeException,
						   UnsupportedEncodingException {
    	
	byte[] encryptedBytes = mECipher.doFinal(inBytes);
    	return encryptedBytes;
        
    } 


    public byte [] decrypt(byte [] strBytes) throws BadPaddingException, 
						    IllegalBlockSizeException,
						    UnsupportedEncodingException, 
						    IOException {

    	byte[] utf8 = mDCipher.doFinal(strBytes);
    	return utf8;
    
    }  	

} /* end EncryptDecrypt.java */

