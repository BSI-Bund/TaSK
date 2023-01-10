/*
 * This file is part of the TLS test tool project.
 * Copyright (C) 2016 Benjamin Eikel <benjamin.eikel@achelos.de>
 *
 * All rights reserved.
 */
#include "ForceCertificateUsage.h"
#include "tls/TlsSession.h"

namespace TlsTestTool {
void ForceCertificateUsage::executePreHandshake(TlsSession & session) {
	if (!session.isClient()) {
		log(__FILE__, __LINE__, "Force sending of a certificate.");
		session.forceCertificateUsage();
	}
}

void ForceCertificateUsage::executePreStep(TlsSession & /*session*/) {
}

void ForceCertificateUsage::executePostStep(TlsSession & /*session*/) {
}

void ForceCertificateUsage::executePostHandshake(TlsSession & /*session*/) {
}
}
