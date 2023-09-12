package com.funkatronics.signer

abstract class Ed25519Signer : Signer {
    override val ownerLength = 32
    override val signatureLength = 64
}