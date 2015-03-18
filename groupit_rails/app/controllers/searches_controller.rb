class SearchesController < ApplicationController
  def show
    @keywords = params[:text].strip.split(' ')
    @like_groups = Group.search(@keywords)
    @quick_join_group = nil

    if @keywords.length == 1
      quick_join = Group.find_by_join_token(@keywords[0])
      if !quick_join.nil?
        @quick_join_group = [quick_join.id, quick_join.name, quick_join.public_group]
      end
    end

    if params[:json] == 'true'
      render json:[[@quick_join_group] + @like_groups]
    end
  end
end
