package au.com.addstar.pansentials;

public interface Module
{
	public void onEnable();
	
	public void onDisable();
	
	public void setPandoraInstance(MasterPlugin plugin);
}
