package com.funkatronics.transaction

import com.funkatronics.encoders.Base64
import com.funkatronics.publickey.SolanaPublicKey
import com.funkatronics.util.asVarint
import kotlin.test.Test
import kotlin.test.assertContentEquals

class MessageTests {

    @Test
    fun testMessageSerialize() {
        // given
        val accounts = listOf(
            SolanaPublicKey(ByteArray(32) {0}),
            SolanaPublicKey(ByteArray(32) {1}),
            SolanaPublicKey(ByteArray(32) {2}),
            SolanaPublicKey(ByteArray(32) {3})
        )

        val blockhash = Blockhash(ByteArray(32) {9})

        val programIdIndex = 1.toUByte()
        val accountAddressIndices = byteArrayOf(0)
        val data = "hello world".encodeToByteArray()
        val instruction = Instruction(programIdIndex, accountAddressIndices, data)

        val message = LegacyMessage(1.toUByte(), 2.toUByte(), 3.toUByte(), accounts, blockhash, listOf(instruction))

        val expectedBytes =
            byteArrayOf(message.signatureCount.toByte(), message.readOnlyAccounts.toByte(), message.readOnlyNonSigners.toByte()) +
                    4.asVarint() + ByteArray(32) {0} + ByteArray(32) {1} + ByteArray(32) {2} + ByteArray(32) {3} +
                    ByteArray(32) {9} +
                    1.asVarint() + programIdIndex.toInt().asVarint() + byteArrayOf(1, 0) + data.size.asVarint() + data

        // when
        val serializedMessage = message.serialize()

        // then
        assertContentEquals(expectedBytes, serializedMessage)
    }

    @Test
    fun testBuildMessage() {
        // given
        val account = SolanaPublicKey(Base64.decode("XJy50755nz75BGthIrxe7XIQ9WkcMxgIOCmqEM30qq4"))
        val programId = SolanaPublicKey.from("MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr")
        val blockhash = Blockhash(ByteArray(32))
        val data = "hello world ".encodeToByteArray()

        val memoInstruction = TransactionInstruction(
            programId,
            listOf(AccountMeta(account, true, true)),
            data
        )

        val memoInstructionTemplate =
            //region sign data
            byteArrayOf(
                0x01.toByte(), // 1 signature required (fee payer)
                0x00.toByte(), // 0 read-only account signatures
                0x01.toByte(), // 1 read-only account not requiring a signature
                0x02.toByte(), // 2 accounts
            ) + account.bytes + programId.bytes + blockhash.bytes +
            //endregion
            //region instructions
            byteArrayOf(
                0x01.toByte(), // 1 instruction (memo)
                0x01.toByte(), // program ID (index into list of accounts)
                0x01.toByte(), // 1 account
                0x00.toByte(), // account index 0
                0x0C.toByte(), // 20 byte payload
            ) + data

        // when
        val message = Message.Builder()
            .addInstruction(memoInstruction)
            .setRecentBlockhash(blockhash)
            .build()

        val serializedMessage = message.serialize()

        // then
        assertContentEquals(memoInstructionTemplate, serializedMessage)
    }
}