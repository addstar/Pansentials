package au.com.addstar.pansentials;

public interface Module
{
	void onEnable();

	void onDisable();

	void setPandoraInstance(MasterPlugin plugin);
}
