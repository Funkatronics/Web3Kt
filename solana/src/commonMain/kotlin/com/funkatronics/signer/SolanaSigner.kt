package com.funkatronics.signer

import com.funkatronics.transaction.Transaction

abstract class SolanaSigner : Ed25519Signer() {
    abstract fun signMessage(message: ByteArray): ByteArray
    abstract fun signTransaction(transaction: ByteArray): ByteArray
    abstract suspend fun signAndSendTransaction(transaction: Transaction): Result<String>
}