class MessagesController < ApplicationController
  def create
		message = Message.new
		message.text = params[:text]
		message.group_id = params[:group_id]
		message.user = current_user
    if user_allow_access?(message.group)
  		if message.save
  			channel_name = "G"+ params[:group_id].to_s
  			message_body = {:text => message.text, :author_name => current_user.name, :author_id =>current_user.id}
  			WebsocketRails[channel_name].trigger(:new, message_body, :namespace => :messages)
        render plain:'OK'
  		else
        render plain:'ERROR'
      end
    else
      render plain:'DENY'
    end
	end
end