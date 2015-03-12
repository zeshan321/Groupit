class MessagesController < ApplicationController
	def create
	message = Message.new
	message.content = params[:text]
	message.group_id = params[:group_id]
	message.user = current_user
	if message.save
		channel_name = "g"+ message.group_id.to_s
		message_body = {:text => message.content, :author_name => message.user.name, :author_id =>}
		puts channel_name
		channel = WebSocketRails([channel_name.to_sym].trigger :new, message_body, :namespace => :messages)
	end
	end
end
