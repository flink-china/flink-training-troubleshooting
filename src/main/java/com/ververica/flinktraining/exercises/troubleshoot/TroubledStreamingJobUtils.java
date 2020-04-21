package com.ververica.flinktraining.exercises.troubleshoot;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class TroubledStreamingJobUtils {
    public static StreamExecutionEnvironment createConfiguredEnvironment(
            final ParameterTool parameters, final boolean local) throws
            IOException, URISyntaxException {
        StreamExecutionEnvironment env;
        if (local) {
            env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());

            String statePath = parameters.get("fsStatePath");
            Path checkpointPath;
            if (statePath != null) {
                FileUtils.deleteDirectory(new File(new URI(statePath)));
                checkpointPath = Path.fromLocalFile(new File(new URI(statePath)));
            } else {
                checkpointPath = Path.fromLocalFile(Files.createTempDirectory("checkpoints").toFile());
            }

            StateBackend stateBackend = new FsStateBackend(checkpointPath);
            env.setStateBackend(stateBackend);
        } else {
            env = StreamExecutionEnvironment.getExecutionEnvironment();
            StateBackend stateBackend = new MemoryStateBackend();
            env.setStateBackend(stateBackend);
        }

        env.getConfig().setGlobalJobParameters(parameters);
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                Integer.MAX_VALUE,
                Time.of(15, TimeUnit.SECONDS) // delay
        ));
        return env;
    }
}
