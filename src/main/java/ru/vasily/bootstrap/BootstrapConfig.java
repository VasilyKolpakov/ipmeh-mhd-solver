package ru.vasily.bootstrap;

import ru.vasily.core.io.RealFileSystem;
import ru.vasily.core.dataobjs.JacksonDataObjService;
import ru.vasily.mydi.AbstractDIConfig;

public class BootstrapConfig extends AbstractDIConfig
{

    @Override
    public void initConfig()
    {
        addImpl(AppStarter.class);
        addImpl(JacksonDataObjService.class);
        addImpl(RealFileSystem.class);
    }

}
