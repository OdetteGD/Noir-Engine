package com.noir.game.engine.navigation;

import java.util.*;

/** Navigation authoring/runtime model for navmesh surfaces, links, agents and dynamic obstacles. */
public final class NavigationWorld {
    public float agentRadius=.35f, agentHeight=1.8f, maxSlope=45f, stepHeight=.35f;
    public boolean dynamicObstacles=true, hierarchicalPathfinding=true;
    public final List<Surface> surfaces=new ArrayList<>();
    public final List<Agent> agents=new ArrayList<>();
    public final List<Link> links=new ArrayList<>();
    public Surface addSurface(String id){Surface s=new Surface(id);surfaces.add(s);return s;}
    public Agent addAgent(String id){Agent a=new Agent(id);agents.add(a);return a;}
    public Link addLink(String id){Link l=new Link(id);links.add(l);return l;}
    public boolean validate(){return agentRadius>0&&agentHeight>0&&maxSlope>0&&maxSlope<=90&&stepHeight>=0;}
    public static final class Surface{public final String id;public boolean enabled=true,baked=false;public int tileSize=64;Surface(String id){this.id=id;}}
    public static final class Agent{public final String id;public float speed=3.5f,acceleration=10f;public boolean avoidance=true;public float stoppingDistance=.25f;Agent(String id){this.id=id;}}
    public static final class Link{public final String id;public boolean enabled=true,bidirectional=true;public float cost=1f;Link(String id){this.id=id;}}
}
