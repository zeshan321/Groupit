class SocketController < WebsocketRails::BaseController
	def client_connected
		send_message :client_connected, 'You are connected to Groupit server'
	end
	
	def authorize_channels
		channel_name = message[:channel]
		channel = WebsocketRails[channel_name]
		if true
			accept_channel
			send_message :client_connected, 'You are authorized for channel:'+channel_name
		end
	end
end
