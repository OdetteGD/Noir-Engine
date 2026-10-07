namespace Noir;

public readonly record struct NetworkPeerId(long Value);
public sealed class NetworkMessage{public NetworkPeerId Sender{get;init;}public byte[] Payload{get;init;}=Array.Empty<byte>();}
public abstract class MultiplayerPeer
{
    public bool IsServer{get;protected set;}public bool IsConnected{get;protected set;}public event Action<NetworkMessage>? PacketReceived;
    protected void RaisePacket(NetworkMessage message)=>PacketReceived?.Invoke(message);
    public virtual void Disconnect(){IsConnected=false;}
}
public sealed class OfflineMultiplayerPeer:MultiplayerPeer{public OfflineMultiplayerPeer(){IsServer=true;IsConnected=true;}}
public static class Multiplayer{public static MultiplayerPeer Peer{get;set;}=new OfflineMultiplayerPeer();public static bool IsServer=>Peer.IsServer;public static bool HasMultiplayerPeer=>Peer.IsConnected;}
