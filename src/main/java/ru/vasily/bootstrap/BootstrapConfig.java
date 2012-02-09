package ru.vasily.bootstrap;

import ru.vasily.core.RealFileSystem;
import ru.vasily.dataobjs.JacksonDataObjService;
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
