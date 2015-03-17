class SocketsController < WebsocketRails::BaseController
	def authorize_channels
		channel_name = message[:channel]
		channel = WebsocketRails[channel_name]
		flag = false
		if channel_name[0] == 'G'
			channel_id = channel_name[1..-1].to_i
			group = Group.find_by(id:channel_id)
			if !group.nil?
				if user_signed_in?
					if group.public_group
						flag = true
					elsif group.users.exists?(id:current_user.id)
						flag = true
					end
				end
			end
		end
		if flag
			accept_channel
			send_message :log, 'Accept channel:'+channel_name
		else
			deny_channel
			send_message :log, 'Deny channel:'+channel_name
		end
	end
end
