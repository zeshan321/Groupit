class MessagesController < ApplicationController
	def create
		message = Message.new
		message.content = params[:text]
		message.group_id = params[:group_id]
		message.user = current_user
		if message.save
			channel_name = "g"+ params[:group_id].to_s
			message_body = {:text => message.content, :author_name => current_user.name, :author_id =>current_user.id}
			WebsocketRails[channel_name].trigger(:new, message_body, :namespace => :messages)
		end
	end
end
