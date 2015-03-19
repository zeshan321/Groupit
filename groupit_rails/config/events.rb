WebsocketRails::EventMap.describe do
	subscribe :client_connected, :to => SocketsController, :with_method => :client_connected
	subscribe :client_disconnected, :to => SocketsController, :with_method => :client_disconnected
	namespace :websocket_rails do
		subscribe :subscribe_private, :to => SocketsController, :with_method => :authorize_channel
	end
end
