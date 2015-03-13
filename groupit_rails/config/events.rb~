WebsocketRails::EventMap.describe do
	subscribe :client_connected, :to => SocketController, :with_method => :client_connected
	
	namespace :websocket_rails do
		subscribe :subscribe_private, :to => SocketController, :with_method => :authorize_channels
	end
	#subscribe :client_connected,:to => ChatController, :with_method => :create
  # You can use this file to map incoming events to controller actions.
  # One event can be mapped to any number of controller actions. The
  # actions will be executed in the order they were subscribed.
  #
  # Uncomment and edit the next line to handle the client connected event:
  #   
  #
  # Here is an example of mapping namespaced events:
  #   namespace :product do
  #     subscribe :new, :to => ProductController, :with_method => :new_product
  #   end
  # The above will handle an event triggered on the client like `product.new`.
end
