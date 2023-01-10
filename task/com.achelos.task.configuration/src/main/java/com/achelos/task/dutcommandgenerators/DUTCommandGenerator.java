package com.achelos.task.dutcommandgenerators;

import java.util.List;

public interface DUTCommandGenerator {
    List<String> connectToServer(final boolean isSessionResumption);
}
