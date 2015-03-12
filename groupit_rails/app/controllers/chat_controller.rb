class ChatController < WebsocketRails::BaseController
	def create
		new_message = Message.new
		new_message.content = message[:text]
		new_message.user = current_user
		if new_message.save
			#WebSocketRails[:chat].trigger :save, "YES", :namespace => :messages
			send_message :save, "YES", :namespace => :messages
		else
			#WebSocketRails[:chat].trigger :save, "NO", :namespace => :messages
			send_message :save, "NO", :namespace => :messages
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
