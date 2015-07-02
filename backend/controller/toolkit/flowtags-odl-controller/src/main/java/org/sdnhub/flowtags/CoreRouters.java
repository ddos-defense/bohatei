package org.sdnhub.flowtags;

import java.util.HashMap;

public class CoreRouters {
	HashMap<String, CoreRouter> routers = null;
	public CoreRouters(){
		this.routers = new HashMap<String, CoreRouter>();
	}

    public int getSize(){
        return routers.size();    
    }   

	public void add(String nodeID, CoreRouter coreRouter){
		this.routers.put(nodeID, coreRouter);
	}
	
	public void clear(){
		this.routers.clear();
	}
	
	public void removeAllFlows(){
		for(CoreRouter router: this.routers.values()){
			router.removeAllFlows();
		}
	}
	
	public void setTagRoute(int tag, int next){
		for(CoreRouter router: this.routers.values()){
			router.setTagRoute(tag, next);
		}
	}
	
	public void setTagRouteWithLocation(int tag, int hostId){
		for(CoreRouter router: this.routers.values()){
			router.setTagRouteWithLocation(tag, hostId);
		}
	}
	
	public void removeTagRoute(int tag){
		for(CoreRouter router: this.routers.values()){
			router.removeTagRoute(tag);
		}
	}
	
	public void removeAllTagRoutes(){
		for(CoreRouter router: this.routers.values()){
			router.removeAllTagRoutes();
		}
	}

	public CoreRouter getRouter(String rid){
		return this.routers.get(rid);
	}
	
	public CoreRouter getRouter(int swID){
		String swIDString = FTUtil.getSWID(swID);
		return this.routers.get(swIDString);
	}
	
	public CoreRouter getRouter(long swID){
		return this.routers.get(FTUtil.getSWID(swID));
	}
	
	public String dumpForwardingTable(){
		StringBuffer bb = new StringBuffer();
		for(String id: this.routers.keySet()){
			bb.append(id +"\n");
			bb.append(this.routers.get(id).dumpForwardingTable());
		}
		return bb.toString();
	}
	
	public void clearForwardingTable(){
		System.out.println("FW Clear: " + this.routers.size());
		for(CoreRouter router: this.routers.values()){
			router.clearForwardingTable();
		}
	}
	
	public boolean hasRouter(String nodeID){
		return this.routers.containsKey(nodeID);
	}

	public void removeRouter(String nodeID){
		this.routers.remove(nodeID);
	}
			
	
	public boolean setIpRoute(int hostId, int dstIP, int mask){
		for(CoreRouter router:this.routers.values()){
			System.out.println(router.node.getNodeIDString());
			if(! router.setIpRoute(hostId, dstIP, mask)){
				continue;
//				return false;
			}
		}
		return true;
	}
}
