class ChatController < WebsocketRails::BaseController
	def create
		new_message = Message.new
		new_message.content = message[:text]
		new_message.user = current_user
		new_message.group_id = message[:group_id]

		if new_message.save
			#WebSocketRails[:chat].trigger :save, "YES", :namespace => :messages
			message_body = {:text => message[:text], :author_name => current_user.name}
			broadcast_message :new, message_body, :namespace => :messages

			#channel_name = ("g"+ message.group_id.to_s).to_sym
			#message_body = {:text => new_message.content, :author_name => new_message.user.name}
			#send_message :save, message_body[:text], :namespace => :messages
		else
			#WebSocketRails[:chat].trigger :save, "NO", :namespace => :messages
			broadcast_message :new, "NO", :namespace => :messages
		end
=begin
		new_messgae.content = message.content
		new_message.group_id = group_id
		new_message.user = current_user
		if new_message.save
			send_message :save, "Message has been saved successfully!", :namespace => :messages
			channel = WebSocketRails[:chat]
			channel.trigger(:new,{:content => new_messgae.content, :author_name => current_user.name},:namespace => :messages)
		else
			send_message :save, "Message has some errors!", :namespace => :messages
		end
=end
	end

	private
	def group_id
		message[:channel]
	end
end
