# DistributedSystems  2022 
## Event Delivery System

## DistributedSys_EventDeliverySystem_2


AppNode.java Is main() for Client. <br>
Broker.java starts serverSocket. <br>

Current version passes a serializable to server and back. <br>
AppNode -> Publisher -> Thread -> Socket <br>

Broker -> Socket -> Thread <br>

UPDATE<br>
publisher init() -> broker init() with BrokerInfo ArrayList update 
