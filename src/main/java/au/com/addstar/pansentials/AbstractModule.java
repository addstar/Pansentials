package au.com.addstar.pansentials;

public abstract class AbstractModule implements Module
{
	private MasterPlugin mPlugin;
	
	@Override
	public void onEnable()
	{
	}
	
	@Override
	public void onDisable()
	{
	}
	
	@Override
	public void setPandoraInstance(MasterPlugin plugin)
	{
		mPlugin = plugin;
	}

    protected MasterPlugin getPlugin()
	{
		return mPlugin;
	}
}
