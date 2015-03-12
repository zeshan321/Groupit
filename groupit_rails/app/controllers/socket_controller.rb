class SocketController < WebsocketRails::BaseController
	def client_connected
		send_message :client_connected, 'welcome to socket world'
	end
	
	def authorize_channels
		puts "authorize_channels"
		channel = WebsocketRails[message[:channel]]
    accept_channel current_user
	end
end
