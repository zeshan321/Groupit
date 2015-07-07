WebsocketRails::EventMap.describe do
	namespace :websocket_rails do
		subscribe :subscribe_private, :to => SocketsController, :with_method => :authorize_channel
	end
end
