/*
 * This file is part of the TLS test tool project.
 * Copyright (C) 2016 Benjamin Eikel <benjamin.eikel@achelos.de>
 *
 * All rights reserved.
 */
#ifndef MANIPULATION_FORCECERTIFICATEUSAGE_H_
#define MANIPULATION_FORCECERTIFICATEUSAGE_H_

#include "Manipulation.h"

namespace TlsTestTool {
/**
 * When picking a certificate to send and no match is found (e.g., wrong key usage), send the first configured
 * certificate instead of failing the handshake. This can be used to force sending of invalid certificates.
 *
 * @author Benjamin Eikel <benjamin.eikel@achelos.de>
 * @date 2016-09-23
 */
class ForceCertificateUsage : public Manipulation {
public:
	virtual void executePreHandshake(TlsSession & session) override;
	virtual void executePreStep(TlsSession & session) override;
	virtual void executePostStep(TlsSession & session) override;
	virtual void executePostHandshake(TlsSession & session) override;
};
}

#endif /* MANIPULATION_FORCECERTIFICATEUSAGE_H_ */
